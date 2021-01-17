import java.sql.*;
import java.util.*;

public class TransactionTesting
{
    public static void main(String[] args)
    {
        final String DB_URL = "jdbc:derby:University";
        Scanner input = new Scanner(System.in);
        
        showOptions();
        
        try
        {
            Connection conn = DriverManager.getConnection(DB_URL);
            while(true)
            {
                System.out.print("\nOption: ");
                int option = input.nextInt();

                if(option == 1)
                    displayDeparmentBudget(conn);
                else if(option == 2)
                    transferBudget(conn);
                else if(option == 3)
                    break;
                else
                    System.out.println("Sorry not an Option");
            }
                
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e);
        }
        
 
    }

    public static void displayDeparmentBudget(Connection conn)
    {
    
        Scanner input = new Scanner(System.in);

        //gets deparment from user
        System.out.println("Deparment Name: ");
        String deptName = input.nextLine();
        
        //Gets the budget of the deparmaent returns -1 if the deparment does not exist
        float budget = getBudget(conn, deptName);
        
        //If the budget is negative the depatment doesn't exist
        if(budget >= 0)
            System.out.println(deptName + " budget: " + budget);
        else    
            System.out.println("That deparment is not in the database");
        

    }

    public static void transferBudget(Connection conn) throws SQLException
    {
        
        Scanner input = new Scanner(System.in);

        //gets deparment source from user
        System.out.print("Source Deparment Name: ");
        String sourceDeptName = input.nextLine();

        //Gets the destination deparment from user
        System.out.print("Destination Deparment Name: ");
        String desDeptName = input.nextLine();

        //Gets the amount to transfer from user
        System.out.print("Amount to Transfer: ");
        float amountToTransfer = input.nextFloat();

        
        try
        {
            //Sets the commit to false
            conn.setAutoCommit(false); 
            
            //Gets the source deparment budget and destiation budget
            float sourceDeptBudget = getBudget(conn, sourceDeptName);
            float desDeptBudget = getBudget(conn, desDeptName);
            
            //Thorws error if the source or destiation deparment doesn't exist
            if(sourceDeptBudget < 0 || desDeptBudget < 0)
                throw new DeparmentDoesNotExistsError("Depatment Does Not Exist");
            
            //If the source depmtement doesn't have another to transfer throw error
            if(sourceDeptBudget < amountToTransfer)
                throw new InsufficientFundsError("Insufficient Funds to transfer.");
            
            
            sourceDeptBudget -= amountToTransfer;
            desDeptBudget += amountToTransfer;
            
            Statement stmt = conn.createStatement();
            //Updates the deparments budget in the database
            stmt.executeUpdate("UPDATE department SET budget = " + sourceDeptBudget +  " where dept_name = '" + sourceDeptName + "'");
            stmt.executeUpdate("UPDATE department SET budget = " + desDeptBudget +  " where dept_name = '" + desDeptName + "'");
            
            //Commits the changes to the data bases
            conn.commit();
            conn.setAutoCommit(true);
      
        }
        catch(DeparmentDoesNotExistsError e)
        {
            System.out.println(e.getMessage());
        }
        catch(InsufficientFundsError e)
        {
            System.out.println(e.getMessage());
        }
        catch(SQLException e)
        {
            //Rolls back the transaction if there is an sql execption
            System.out.println(e.getMessage() + " Transaction rolled backed.");
            conn.rollback();
        }
    }

    public static float getBudget(Connection conn, String deptName)
    {
        //Returns budget of deparment of -1 if no deparment in the database
        try
        {
            Statement stmt = conn.createStatement();
            //Looks up user in data base
            ResultSet resultSet = stmt.executeQuery("SELECT budget FROM department where dept_name = '" + deptName + "'");
            
            if(resultSet.next())
            {
                return resultSet.getFloat("budget");
            }
            
            return -1;
      
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    public static void showOptions()
    {
        //Prints out the options and the number to pick them
        System.out.println("Options");
        System.out.println("-----------");
        System.out.println("Show Deparment Budget (1)");
        System.out.println("Transfer Budgets(2)");
        System.out.println("Quit(3)\n\n");

    }

    //Custom error classes
    public static class InsufficientFundsError extends Exception { 
        public InsufficientFundsError(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class DeparmentDoesNotExistsError extends Exception { 
        public DeparmentDoesNotExistsError(String errorMessage) {
            super(errorMessage);
        }
    }

}