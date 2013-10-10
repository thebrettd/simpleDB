import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SimpleDatabase{

    private Map<String,Stack<Integer>> variableToValueMap;
    private Map<Integer, Stack<ArrayList<String>>> valueToVariableMap;
    private int transactionCount;

    public SimpleDatabase(){
        valueToVariableMap = new TreeMap<Integer,Stack<ArrayList<String>>>();
        variableToValueMap = new TreeMap<String,Stack<Integer>>();
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

    public void set(String variableName, Integer value){
        setVariableToValueMap(variableName, value);
        setValueToVariableMap(variableName, value);
    }

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


    public void unset(String variableName) {
        unsetValueToVariableMap(variableName); //Do this first, because it has a dependency on variableToValueMap data
        unsetVariableToValueMap(variableName);
    }

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

    public Integer numEqualTo(Integer valueToFind){
        Stack<ArrayList<String>> arrayLists = valueToVariableMap.get(valueToFind);
        if (arrayLists != null){
            ArrayList<String> currTransactionCounts = arrayLists.peek();
            return currTransactionCounts.size();
        }else{
            return 0;
        }

    }

    public void begin(){
        transactionCount++;
    }

    public void rollback(){
        if (transactionCount == 0){
            System.out.println("NO TRANSACTION");
        }else{
            rollbackVariableToValueMap();
            rollbackValueToVariableMap();
            transactionCount--;
        }
    }

    private void rollbackValueToVariableMap() {
        for(Integer value : valueToVariableMap.keySet()){
            Stack<ArrayList<String>> variablesWithValue = valueToVariableMap.get(value);
            if (variablesWithValue != null && variablesWithValue.size() > transactionCount){
                variablesWithValue.pop();
            }
        }
    }

    private void rollbackVariableToValueMap() {
        for(String variable : variableToValueMap.keySet()){
            Stack<Integer> values = variableToValueMap.get(variable);
            if (values != null && values.size() > transactionCount){
                values.pop();
            }
        }
    }

    public void commit(){
        if (transactionCount == 0){
            System.out.println("NO TRANSACTION");
        }else{
            transactionCount = 0;
            commitVariableToValueMap();
            commitValueToVariableMap();
        }
    }

    private void commitValueToVariableMap() {
        for(Integer value : valueToVariableMap.keySet()){
            Stack<ArrayList<String>> committedValues = new Stack<ArrayList<String>>();
            ArrayList<String> valuesToCommit = valueToVariableMap.get(value).peek();
            committedValues.push(valuesToCommit);
            valueToVariableMap.put(value,committedValues);
        }
    }

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

    public class GetResult{

        private Integer myVal;

        GetResult(Integer val){
            myVal = val;
        }

        GetResult(){

        }

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
