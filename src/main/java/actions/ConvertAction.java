package actions;

import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import parser.GraphQLParser;

import java.util.HashMap;

public class ConvertAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        //Get required data keys
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        //Set visibility only in case of existing project and editor
        e.getPresentation().setVisible(project != null && editor != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        /* Project must be set for file chooser */
        if(project != null) {

            /* Create a file chooser solely for graphql files */
            TreeFileChooser fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
                    "Choose GraphQL Scheme file (*.graphql)",
                    null,
                    null,
                    psiFile -> psiFile.getVirtualFile().getExtension().contains("graphql"),
                    true,
                    false
            );

            /* Show it and return selected file */
            fileChooser.showDialog();
            PsiFile selectedFile = fileChooser.getSelectedFile();

            /* If any file was selected */
            if(selectedFile != null) {

                /* Output text template to work with */
                String template =
                        "data class {{className}} (\n"+
                            "{{attributes}}\n"+
                        ")";

                /* Initialize attribute conversion mappings */
                HashMap<String, String> attributeMappings = new HashMap<>();
                attributeMappings.put("[String]", "List<String>");
                attributeMappings.put("Float!", "Float");
                attributeMappings.put("Float", "Float?");

                /* Create new GraphQLParser and let it fill the template */
                String output = new GraphQLParser(selectedFile.getText(), template)
                        .readClassName()
                        .readAttributes(attributeMappings)
                        .getString();

                /* Get open editor instance */
                FileEditorManager manager = FileEditorManager.getInstance(project);
                final Editor editor = manager.getSelectedTextEditor();

                /* If an editor is opened, write to it */
                if(editor != null) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        EditorModificationUtil.insertStringAtCaret(editor, output, true, true);
                    });
                }
            }
        }
    }
}
