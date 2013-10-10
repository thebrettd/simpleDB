import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SimpleDatabase{

    private Map<String,Stack<Integer>> variableToValueMap;
    private Map<Integer, Stack<ArrayList<String>>> valueToVariableMap;
    private int transactionCount;

    public SimpleDatabase(){
        //Map from a given Integer value to a list of variables that all share the same value.
        valueToVariableMap = new TreeMap<Integer,Stack<ArrayList<String>>>();
        //Map from a given String variable to its value
        variableToValueMap = new HashMap<String,Stack<Integer>>();
        transactionCount = 0;
    }

    private enum Operations {SET,UNSET,GET,NUMEQUALTO,BEGIN,COMMIT,ROLLBACK,END}
    public static void main(String[] args) {

        SimpleDatabase myDatabase = new SimpleDatabase();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            try {
                String input = br.readLine();
                String[] inputs = input.split(" ");

                Operations operation;
                try{
                   operation = Operations.valueOf(inputs[0]);
                    switch(operation){
                        case SET: myDatabase.set(inputs[1],Integer.parseInt(inputs[2])); break;
                        case UNSET: myDatabase.unset(inputs[1]); break;
                        case GET:
                            System.out.println(myDatabase.get(inputs[1])); break;
                        case NUMEQUALTO: System.out.println(myDatabase.numEqualTo(Integer.parseInt(inputs[1]))); break;
                        case BEGIN: myDatabase.begin(); break;
                        case COMMIT: myDatabase.commit(); break;
                        case ROLLBACK: myDatabase.rollback(); break;
                        case END: myDatabase.end(); break;
                        default:
                            System.out.println("Unknown operation " + inputs[0]);
                    }
                }catch(IllegalArgumentException e){
                    System.out.println("Invalid operation entered");
                }
            } catch (IOException e) {
                System.out.println("Error reading operation from stdin");
            }

        }

    }

    public void end(){
        System.exit(0);
    }

    /***
     * Set the value in both data structures
     * O(log n)
     *
     * @param variableName
     * @param value
     */
    public void set(String variableName, Integer value){
        setVariableToValueMap(variableName, value);
        setValueToVariableMap(variableName, value);
    }

    /***
     * Set the value in the valueToVariable map.
     * O(log n) with respect to the number of items in the database, because it fetches the value using a TreeMap.
     *
     *  Note: at worst case this is linear with respect to the number of open transactions,because it may need to
     *  push a value onto the stack for each previous transaction where the value was not touched. The number of
     *  open transactions is assumed to be small.
     *
     * @param variableName
     * @param value
     */
    private void setValueToVariableMap(String variableName, Integer value) {
        Stack<ArrayList<String>> values = valueToVariableMap.get(value);
        if (values == null){
            values = new Stack<ArrayList<String>>();
            for (int i=0;i<transactionCount;i++){
                values.push(null);
            }
            ArrayList<String> valuesInCurrTransaction = new ArrayList<String>();
            valuesInCurrTransaction.add(variableName);
            values.push(valuesInCurrTransaction);
            valueToVariableMap.put(value,values);
        }else {
            if (values.size() < transactionCount+1){
                for(int i=0;i<values.size();i++){
                    ArrayList<String> valuesInOldTransaction = values.peek();
                    values.push(valuesInOldTransaction);
                }
                ArrayList<String> valuesInCurrentTransaction = values.peek();
                valuesInCurrentTransaction.add(variableName);
                values.push(valuesInCurrentTransaction);
            }else if(values.size() == transactionCount +1){
                ArrayList<String> valuesInCurrTransaction = values.peek();
                valuesInCurrTransaction.add(variableName);
            }
        }
    }

    /***
     * Set the value in the variableToValue map
     * O(c) with respect to the number of items in the database, because it fetches the value using a hashMap.
     *
     *  Note: at worst case this is linear with respect to the number of open transactions,because it may need to
     *  push a value onto the stack for each previous transaction where the value was not touched. The number of
     *  open transactions is assumed to be small.
     *
     * @param variableName
     * @param value
     */
    private void setVariableToValueMap(String variableName, Integer value) {
        Stack<Integer> values = variableToValueMap.get(variableName);
        if (values == null){
            values = new Stack<Integer>();
            for(int i=0;i<transactionCount;i++){
                values.push(null);
            }
        }
        values.push(value);
        variableToValueMap.put(variableName, values);
    }

    /***
     * Return the value of the variable by looking it up in a HashMap.
     * O(c) (constant) with respect to the number of items in the database
     *
     * @param variableName
     * @return
     */
    public GetResult get(String variableName){

        if(variableToValueMap.containsKey(variableName)){
            Stack<Integer> values = variableToValueMap.get(variableName);
            try{
                return new GetResult(values.peek());
            }catch(EmptyStackException e){
                return new GetResult();
            }

        }else{
            return new GetResult();
        }
    }

    /***
     * Set the value to null in both data structures
     * O(log n) with respect to the number of items in the database.
     *
     * @param variableName
     */
    public void unset(String variableName) {
        unsetValueToVariableMap(variableName); //Do this first, because it has a dependency on variableToValueMap data
        unsetVariableToValueMap(variableName);
    }

    /***
     * Set the value to null in valueToVariable map.
     * O(log n) with respect to the number of items in the database, because it must lookup the value in the TreeMap
     *
     * Note: at worst case this is linear with respect to the number of open transactions, because it may need to push
     * a value onto the stack for each previous transaction where the variables were not touched. The number of
     *  open transactions is assumed to be small.
     *
     * @param variableName
     */
    private void unsetValueToVariableMap(String variableName) {
        GetResult getResult = get(variableName);
        if ((getResult.toString().equals("NULL"))){
            //Variable not set, nothing to do
        }else{
            Stack<ArrayList<String>> variablesWithValue = valueToVariableMap.get(Integer.parseInt(getResult.toString()));
            if(variablesWithValue.size() < transactionCount + 1){
                for(int i=variablesWithValue.size();i<transactionCount+1 ;i++){
                    variablesWithValue.push(new ArrayList<String>(variablesWithValue.peek()));
                }
            }

            ArrayList<String> variablesWithValueInTransaction = variablesWithValue.pop();
            variablesWithValueInTransaction.remove(variableName);
            variablesWithValue.push(variablesWithValueInTransaction);
        }
    }

    /***
     * Set the value to null in the variableToValue map.
     * O(c) with respect to the number of items in the database, because it fetches the value using a hashMap.
     *
     *  Note: at worst case this is linear with respect to the number of open transactions,because it may need to
     *  push a value onto the stack for each previous transaction where the value was not touched. The number of
     *  open transactions is assumed to be small.
     *
     * @param variableName
     */
    private void unsetVariableToValueMap(String variableName) {
        Stack<Integer> values = variableToValueMap.get(variableName);
        if (values != null){
            if (values.size() == transactionCount + 1){
                values.push(null);
            }else if (values.size() < transactionCount + 1) {
                for(int i=values.size(); i < transactionCount; i++){
                    values.push(values.peek());
                }
                values.push(null);
            } else {
                throw new IllegalStateException("Values.size() > transaction count??");
            }
        }
    }

    /***
     * Return the number of variables whose value equals the Integer passed in.
     * O(log n) because valueToVariable map is a TreeMap (Red-black binary search tree)
     * @param valueToFind
     * @return
     */
    public Integer numEqualTo(Integer valueToFind){
        Stack<ArrayList<String>> arrayLists = valueToVariableMap.get(valueToFind);
        if (arrayLists != null){
            ArrayList<String> currTransactionCounts = arrayLists.peek();
            return currTransactionCounts.size();
        }else{
            return 0;
        }

    }

    /***
     * Increment the transaction count
     */
    public void begin(){
        transactionCount++;
    }

    /***
     * Rollback the most recent uncommitted operation in both data structures
     * O(2n) with respect to the number of items in the database
     */
    public void rollback(){
        if (transactionCount == 0){
            System.out.println("NO TRANSACTION");
        }else{
            rollbackVariableToValueMap();
            rollbackValueToVariableMap();
            transactionCount--;
        }
    }

    /***
     * Rollback the most recent uncommitted operation in the valueToVariable map.
     * O(n) with respect to the number of values in the database.
     *
     * Iterate over all of the keys, pop off the ArrayList if uncommitted values are found
     */
    private void rollbackValueToVariableMap() {
        for(Integer value : valueToVariableMap.keySet()){
            Stack<ArrayList<String>> variablesWithValue = valueToVariableMap.get(value);
            if (variablesWithValue != null && variablesWithValue.size() > transactionCount){
                variablesWithValue.pop();
            }
        }
    }

    /***
     * Rollback the most recent uncommitted changes in the variableToValue map.
     * O(n) with respect to the number of items in the database
     *
     * Iterate over all of the keys, pop off the stack if an uncommitted value is found
     */
    private void rollbackVariableToValueMap() {
        for(String variable : variableToValueMap.keySet()){
            Stack<Integer> values = variableToValueMap.get(variable);
            if (values != null && values.size() > transactionCount){
                values.pop();
            }
        }
    }

    /***
     * Commit the values set in the most recent transaction to both data structures
     * O(2n) with respect to the number of items in the database
     */
    public void commit(){
        if (transactionCount == 0){
            System.out.println("NO TRANSACTION");
        }else{
            transactionCount = 0;
            commitVariableToValueMap();
            commitValueToVariableMap();
        }
    }

    /***
     * Commit all uncommitted changes in the valueToVariable map
     * O(n) with respect to the number of items in the database
     *
     * Iterate over the keySet(). The ArrayList found at the top of the stack for each value is used as the seed for a
     * new Stack for each value.
     */
    private void commitValueToVariableMap() {
        for(Integer value : valueToVariableMap.keySet()){
            Stack<ArrayList<String>> committedValues = new Stack<ArrayList<String>>();
            ArrayList<String> valuesToCommit = valueToVariableMap.get(value).peek();
            committedValues.push(valuesToCommit);
            valueToVariableMap.put(value,committedValues);
        }
    }

    /***
     * Commit all uncommitted changes in the variableToValue map
     * O(n) with respect to the number of items in the database
     *
     * Iterate over the keySet(). The last value on the stack (if not null) is used as the seed for a new stack for each
     * variable.
     */
    private void commitVariableToValueMap() {
        for(String key : variableToValueMap.keySet()){
            Stack<Integer> valuesForKey = variableToValueMap.get(key);
            if (valuesForKey != null){
                Integer valueToCommit = valuesForKey.pop();
                    if (valueToCommit != null){
                        Stack<Integer> newValues = new Stack<Integer>();
                        newValues.push(valueToCommit);
                        variableToValueMap.put(key,newValues);
                    }
            }

        }
    }

    /***
     * Wrapper class for Database Get result
     */
    public class GetResult{

        private Integer myVal;

        GetResult(Integer val){
            myVal = val;
        }

        GetResult(){}

        @Override
        public String toString() {
            if (myVal != null){
                return myVal.toString();
            }else{
                return "NULL";
            }
        }
    }

}
