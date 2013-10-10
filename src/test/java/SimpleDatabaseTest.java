import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SimpleDatabaseTest {

    SimpleDatabase myTestDatabase;

    @Before
    public void initialize() {
        myTestDatabase = new SimpleDatabase();
    }

    @Test
    public void testNumEqualTo() {
        assertTrue(myTestDatabase.numEqualTo(10) == 0);

        myTestDatabase.set("A",10);
        assertTrue(myTestDatabase.numEqualTo(10) == 1);
        myTestDatabase.set("B",10);
        assertTrue(myTestDatabase.numEqualTo(10) == 2);
        myTestDatabase.unset("B");
        assertTrue(myTestDatabase.numEqualTo(10) == 1);
    }

    @Test
    public void testUnset() {
        myTestDatabase.set("A",10);
        assertTrue(myTestDatabase.get("A").toString().equals("10"));

        myTestDatabase.unset("A");
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
    }

    @Test
    public void testCommit(){
        myTestDatabase.commit();
        myTestDatabase.begin();
        myTestDatabase.set("A",10);
        myTestDatabase.commit();
        assertTrue(myTestDatabase.get("A").toString().equals("10"));
        myTestDatabase.rollback();

    }

    @Test
    public void testSetOutsideTransactionIsCommitted(){
        myTestDatabase.set("A",10);
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.get("A").toString().equals("10"));
    }

    @Test
    public void testUnsetInTransaction(){
        myTestDatabase.begin();
        myTestDatabase.set("A",10);
        myTestDatabase.begin();
        myTestDatabase.unset("A");
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.get("A").toString().equals("10"));

    }

    @Test
    public void testGetSomethingNotSetIsNull() {
        assertTrue(myTestDatabase.get("B").toString().equals("NULL"));
    }

    @Test
    public void testGetSomethingSetReturnsValue() {
        myTestDatabase.set("A",10);
        assertTrue(myTestDatabase.get("A").toString().equals("10"));
    }

    @Test
    public void testSet() {
        myTestDatabase.set("A",10);
    }

    @Test
    public void testBegin(){
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
        myTestDatabase.begin();
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
        myTestDatabase.set("A",10);
        assertTrue(myTestDatabase.get("A").toString().equals("10"));
        myTestDatabase.begin();
        myTestDatabase.set("A",20);
        assertTrue(myTestDatabase.get("A").toString().equals("20"));

    }

    @Test
    public void testRollback(){
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
        myTestDatabase.begin();
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
        myTestDatabase.set("A",10);
        assertTrue(myTestDatabase.get("A").toString().equals("10"));
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
    }

    @Test
    public void transactionTestCase1(){
        myTestDatabase.begin();
        myTestDatabase.set("a",10);
        assertTrue(myTestDatabase.get("a").toString().equals("10"));
        myTestDatabase.begin();
        myTestDatabase.set("a",20);
        assertTrue(myTestDatabase.get("a").toString().equals("20"));
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.get("a").toString().equals("10"));
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.get("a").toString().equals("NULL"));
    }

    @Test
    public void transactionTestCase2(){
        myTestDatabase.begin();
        myTestDatabase.set("a",30);
        myTestDatabase.begin();
        myTestDatabase.set("a",40);
        myTestDatabase.commit();
        assertTrue(myTestDatabase.get("a").toString().equals("40"));
        myTestDatabase.rollback();
    }

    @Test
    public void transactionTestCase3(){
        myTestDatabase.set("A",50);
        myTestDatabase.begin();
        assertTrue(myTestDatabase.get("A").toString().equals("50"));
        myTestDatabase.set("A",60);
        myTestDatabase.begin();
        myTestDatabase.unset("A");
        assertTrue(myTestDatabase.get("A").toString().equals("NULL"));
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.get("A").toString().equals("60"));
        myTestDatabase.commit();
        assertTrue(myTestDatabase.get("A").toString().equals("60"));
    }

    @Test
    public void testCase2(){
        myTestDatabase.set("A",10);
        myTestDatabase.begin();
        assertTrue(myTestDatabase.numEqualTo(10) == 1);
        myTestDatabase.begin();
        myTestDatabase.unset("A");
        assertTrue(myTestDatabase.numEqualTo(10) == 0);
        myTestDatabase.rollback();
        assertTrue(myTestDatabase.numEqualTo(10) == 1);
    }

    @Test
    public void testCommitFromAllOpenTransactions(){
        myTestDatabase.begin();
        myTestDatabase.set("a",10);
        myTestDatabase.begin();
        myTestDatabase.set("b",20);
        myTestDatabase.commit();
        assertTrue(myTestDatabase.get("a").toString().equals("10"));
        assertTrue(myTestDatabase.get("b").toString().equals("20"));
    }

}
