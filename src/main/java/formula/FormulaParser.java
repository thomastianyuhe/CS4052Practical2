package formula;

import java.util.*;
import java.io.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import formula.pathFormula.*;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.*;

/**
 * Class used to parse formulas from JSON. Create a instance of this class
 * giving the path to the JSON file to parse as a parameter to the constructor.
 * Call parse to get the formula as a Java instance. Also use static method
 * parseRawFormulaString() if you which to directly parse a formula from a given
 * string.
 */
public class FormulaParser {
    private static final String JSON_FORMULA_FIELD = "formula";
    public static final char FALSE_TOKEN_PREFIX = 'F';
    public static final char TRUE_TOKEN_PREFIX = 'T';
    public static final char NOT_TOKEN = '!';
    public static final char UNTIL_TOKEN = 'U';
    public static final char AND_TOKEN = '&';
    public static final char OR_TOKEN = '|';
    public static final char EVENTUALLY_TOKEN = 'F';
    public static final char RIGHT_BRACKET_TOKEN = ')';
    public static final char LEFT_BRACKET_TOKEN = '(';
    public static final char NEXT_TOKEN = 'X';
    public static final char ALWAYS_TOKEn = 'G';
    public static final char THEREEXISTS_TOKEN = 'E';
    public static final char FORALL_TOKEN = 'A';
    private Reader reader;
    Gson gson = new Gson();
    private JsonObject jsonFormula;

    public FormulaParser(String filePath) throws IOException {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(new FileReader(filePath));
        jsonFormula = jsonElement.getAsJsonObject();
        String formula = jsonFormula.get(JSON_FORMULA_FIELD).getAsString();
        reader = new Reader(formula);
    }

    private FormulaParser() {
        jsonFormula = null;
        reader = null;
    }

    public StateFormula parse() throws IOException {
        try {
            return recursiveParseStateFormula();
        } catch (IOException e) {
            throw new IOException("Error at character position " + reader.getPosition() + ":\n" + e.getMessage());
        }
    }

    /**
     * Parses a formula from the given string. Does not expect JSON, only the
     * formula string itself. For this reason, no action constraints may be
     * included using this method. Action constraints must be specified in JSON
     * and parsed by creating an instance of this object.
     * 
     * @param formula
     * @return
     * @throws IOException
     */
    public static StateFormula parseRawFormulaString(String formula) throws IOException {
        FormulaParser parser = new FormulaParser();
        parser.reader = new Reader(formula);
        return parser.parse();
    }

    public StateFormula recursiveParseStateFormula() throws IOException {
        char nextChar = reader.nextChar();
        if (nextChar == LEFT_BRACKET_TOKEN) {
            return recursiveParseStateFormulaHelper();
        } else {
            return parseStateFormula(nextChar);
        }
    }

    private StateFormula recursiveParseStateFormulaHelper() throws IOException {
        StateFormula stateFormula;
        StateFormula subformula = recursiveParseStateFormula();
        char nextChar = reader.nextChar();
        switch (nextChar) {
        case RIGHT_BRACKET_TOKEN:
            return subformula;
        case OR_TOKEN: {
            validateNextChars(OR_TOKEN);
            StateFormula subformula2 = recursiveParseStateFormula();
            stateFormula = new Or(subformula, subformula2);
        }
            break;
        case AND_TOKEN: {
            validateNextChars(AND_TOKEN);
            StateFormula subformula2 = recursiveParseStateFormula();
            stateFormula = new And(subformula, subformula2);
        }
            break;
        default:
            throw new IOException("unexpected character '" + nextChar + "'");
        }
        validateNextChars(RIGHT_BRACKET_TOKEN);
        return stateFormula;
    }

    private StateFormula parseStateFormula(char nextChar) throws IOException {
        switch (nextChar) {
        case NOT_TOKEN:
            return new Not(recursiveParseStateFormula());
        case FORALL_TOKEN:
            return new ForAll(parsePathFormula());
        case THEREEXISTS_TOKEN:
            return new ThereExists(parsePathFormula());
        case TRUE_TOKEN_PREFIX:
            validateNextChars("RUE".toCharArray());
            return new BoolProp(true);
        case FALSE_TOKEN_PREFIX:
            validateNextChars("ALSE".toCharArray());
            return new BoolProp(false);
        default:
            if (isLowerCase(nextChar)) {
                reader.unread(nextChar);
                String ident = parseOptionalIdentifier(true);
                if (ident != null) {
                    return new AtomicProp(ident);
                }
            }
            throw new IOException("Expected state formula at this position.");
        }
    }

    private PathFormula parsePathFormula() throws IOException {
        String actionSet1Identifier = parseOptionalIdentifier(true);
        Set<String> actionSet1 = getActions(actionSet1Identifier);
        char nextChar = reader.nextChar();
        switch (nextChar) {
        case ALWAYS_TOKEn:
            return new Always(recursiveParseStateFormula(), actionSet1);
        case NEXT_TOKEN:
            return new Next(recursiveParseStateFormula(), actionSet1);
        case EVENTUALLY_TOKEN:
            String actionSet2Identifier = parseOptionalIdentifier(false);
            Set<String> actionSet2 = getActions(actionSet2Identifier);
            return new Eventually(recursiveParseStateFormula(), actionSet1, actionSet2);
        case LEFT_BRACKET_TOKEN:
            Until until = parseUntil();
            validateNextChars(RIGHT_BRACKET_TOKEN);
            return until;
        default:
            throw new IOException("Expected path quantifier");
        }
    }

    private Until parseUntil() throws IOException {
        StateFormula leftFormula = recursiveParseStateFormula();
        String actionSet1Identifier = parseOptionalIdentifier(true);
        validateNextChars(UNTIL_TOKEN);
        String actionSet2Identifier = parseOptionalIdentifier(false);
        StateFormula rightFormula = recursiveParseStateFormula();
        Set<String> actionSet1 = getActions(actionSet1Identifier);
        Set<String> actionSet2 = getActions(actionSet2Identifier);
        return new Until(leftFormula, rightFormula, actionSet1, actionSet2);
    }

    private void validateNextChars(char... chars) throws IOException {
        for (char charIn : chars) {
            char nextChar = reader.nextChar();
            if (nextChar != charIn) {
                reader.unread(nextChar);
                throw new IOException("expected '" + charIn + "' but found '" + nextChar + "'");
            }
        }
    }

    /**
     * Parses sequence of lower case characters into a string. Keeps reading
     * from stream until the next character to be read is not in the range a-z.
     * 
     * @return The string of lower case characters or null to denote that the
     *         reader found no lower case characters at its position.
     * @throws IOException
     */
    public String parseOptionalIdentifier(boolean allowWhitespacePrefix) throws IOException {
        char nextChar;
        if (allowWhitespacePrefix) {
            nextChar = reader.nextChar();
        } else {
            nextChar = reader.rawRead();
        }
        if (isLowerCase(nextChar) || isNumericDigit(nextChar)) {
            StringBuilder buffer = new StringBuilder(nextChar + "");
            while (reader.ready()) {
                nextChar = reader.rawRead();
                if (isLowerCase(nextChar) || isNumericDigit(nextChar)) {
                    buffer.append(nextChar + "");
                } else {
                    reader.unread(nextChar);
                    break;
                }
            }
            return buffer.toString();
        } else {
            reader.unread(nextChar);
            return null;
        }
    }

    private boolean isNumericDigit(char nextChar) {
        return nextChar >= '0' && nextChar <= '9';
    }

    public boolean isLowerCase(char charIn) {
        return (charIn >= 'a' && charIn <= 'z');
    }

    private Set<String> getActions(String actionSetIdentifier) {
        if (actionSetIdentifier == null) {
            return new HashSet<String>();
        }
        String[] actionsArray = gson.fromJson(jsonFormula.get(actionSetIdentifier), String[].class);
        if (actionsArray == null) {
            return new HashSet<String>();
        } else {
            return new HashSet<String>(Arrays.asList(actionsArray));
        }
    }

}
