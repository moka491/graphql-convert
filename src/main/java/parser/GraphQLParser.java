package parser;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphQLParser {

    //region Properties
    private String graphqlInput;
    private String output;
    //endregion

    //region Constructor
    public GraphQLParser(String graphqlInput, String outputTemplate) {
        this.graphqlInput = graphqlInput;
        this.output = outputTemplate;
    }
    //endregion

    //region Parsing Functions

    /**
     * Parses the GraphQL Schema Type name
     * into the {{className}} tag
     * @return GraphQLParser self reference
     */
    public GraphQLParser readClassName() {

        Matcher matcher = Pattern.compile("(?:\\s|^)(\\w+)\\s+?\\{").matcher(graphqlInput);

        if(matcher.find()) {
            String className = matcher.group(1);
            replaceTag("className", className);
        }

        return this;
    }

    /**
     * Parses the list of GraphQL attributes
     * into the {{attributes}}
     * It also uses a conversion hashmap to convert
     * data types from GraphQL to the destination language
     * @param dataTypeConversions A key-value list of source and destination data types
     * @return GraphQLParser self reference
     */
    public GraphQLParser readAttributes(@Nullable HashMap<String, String> dataTypeConversions) {

        Matcher matcher = Pattern.compile("(?:\\s|^)(\\w+)\\s?:\\s?(\\w+)").matcher(graphqlInput);

        String newAttributes = "";
        int matchCount = 0;

        while(matcher.find()) {

            if(matchCount > 0) {
                newAttributes += ",\n";
            }

            matchCount++;

            String attrName = matcher.group(1);
            String attrType = matcher.group(2);

            if(dataTypeConversions != null && dataTypeConversions.containsKey(attrType)) {
                attrType = dataTypeConversions.get(attrType);
            }

            newAttributes += "val "+attrName+": "+attrType;
        }

        if(matchCount > 0) {
            replaceTag("attributes", newAttributes);

        }
        return this;
    }

    //endregion

    //region Output Functions
    /**
     * Returns the output of the parser in the current state.
     * This consists of the template given at initialization,
     * with the {{tags}} replaced by their actual content,
     * if the appropriate Parser functions where called beforehand.
     * @return String The output of the parser
     */
    public String getString() {
        return output;
    }
    //endregion

    //region Private Functions
    /**
     * Replaces a {{tag}} inside the output with the desired content.
     * @param tagName The name of the tag without curly braces
     * @param content The content to replace it with
     */
    private void replaceTag(String tagName, String content) {
        String tag = "{{"+tagName+"}}";

        if(output.contains(tag)) {
            output = output.replace(tag, content);
        }
    }
    //endregion
}
