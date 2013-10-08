import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SimpleDatabase{

    private Map<String,Stack<Integer>> variableToValueMap;
    private int transactionCount;

    public SimpleDatabase(){
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
                switch(Operations.valueOf(inputs[0])){
                    case SET: myDatabase.set(inputs[1],Integer.parseInt(inputs[2])); break;
                    case UNSET: myDatabase.unset(inputs[1]); break;
                    case GET:
                        System.out.println(myDatabase.get(inputs[1])); break;
                    case NUMEQUALTO: myDatabase.numEqualTo(Integer.parseInt(inputs[1])); break;
                    case BEGIN: myDatabase.begin(); break;
                    case COMMIT: myDatabase.commit(); break;
                    case ROLLBACK: myDatabase.rollback(); break;
                    case END: myDatabase.end(); break;
                    default:
                        System.out.println("Unknown operation " + inputs[0]);
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
            return new GetResult(values.peek());
        }else{
            return new GetResult();
        }
    }


    public void unset(String variableName) {

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
        int numFound = 0;

        Integer currVal;
        for(Stack<Integer> valueList : variableToValueMap.values()){
            currVal = valueList.peek();
            if (currVal != null && currVal.equals(valueToFind)){
                numFound++;
            }
        }
        return numFound;
    }

    public void begin(){
        transactionCount++;
    }

    public void rollback(){
        if (transactionCount == 0){
            System.out.println("NO TRANSACTION");
        }else{
            for(String variable : variableToValueMap.keySet()){
                Stack<Integer> values = variableToValueMap.get(variable);
                if (values != null && values.size() > transactionCount){
                    values.pop();
                }
            }
            transactionCount--;
        }
    }

    public void commit(){
        if (transactionCount == 0){
            System.out.println("NO TRANSACTION");
        }else{
            transactionCount = 0;
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
