package restaurant;

import java.util.Arrays;
import java.util.Objects;

/**
 * RUHungry is a fictitious restaurant.
 * You will be running RUHungry for a day by seating guests,
 * taking orders, donation requests and restocking the pantry as necessary.
 *
 * Compiling and executing:
 * 1. use the run or debug function to run the driver and test your methods
 *
 * @author Mary Buist
 * @author Kushi Sharma
 */

public class RUHungry {

    /*
     * Instance variables
     */

    // Menu: two parallel arrays. The index in one corresponds to the same index in the other.
    private String[] categoryVar; // array where containing the name of menu categories (e.g. Appetizer, Dessert).
    private MenuNode[] menuVar;   // array of lists of MenuNodes where each index is a category.

    // Stock: hashtable using chaining to resolve collisions.
    private StockNode[] stockVar;  // array of linked lists of StockNodes (use hashfunction to organize Nodes: id % stockVarSize)
    private int stockVarSize;

    // Transactions: orders, donations, restock transactions are recorded
    private TransactionNode transactionVar; // refers to the first front node in linked list

    // Queue keeps track of people who've left the restaurant
    private Queue<People> leftQueueVar;

    // Tables
    private People[] tables;        // array for people who are currently sitting
    private int[][]  tablesInfo;    // 2-D integer array where the first row contains how many seats there are at each table (each index)
    // and the second row contains "0" or "1", where 1 is the table is not available and 0 the opposite

    /*
     * Default constructor
     */
    public RUHungry () {
        categoryVar    = null;
        menuVar        = null;
        stockVar       = null;
        stockVarSize   = 0;
        transactionVar = null;
        leftQueueVar   = null;
        tablesInfo     = null;
        tables         = null;
    }

    /*
     * Get/Set methods
     */
    public MenuNode[] getMenu() { return menuVar; }
    public String[] getCategoryArray() { return categoryVar;}
    public StockNode[] getStockVar() { return stockVar; }
    public TransactionNode getFrontTransactionNode() { return transactionVar; }
    public TransactionNode resetFrontNode() {return transactionVar = null;} // method to reset the transactions for a new day
    public Queue<People> getLeftQueueVar() { return leftQueueVar; }
    public int[][] getTablesInfo() { return tablesInfo; }

    /*
     * Menu methods
     */
    public void menu(String inputFile) {
        StdIn.setFile(inputFile);
        int numCategories = StdIn.readInt(); // Read number of categories

        categoryVar = new String[numCategories];
        menuVar = new MenuNode[numCategories];

        for (int i = 0; i < numCategories; i++) {
            StdIn.readLine(); // Read and discard the newline character after the number of categories or dishes
            categoryVar[i] = StdIn.readLine(); // Read category name

            int numDishes = StdIn.readInt(); // Read number of dishes in this category
            MenuNode categoryHead = null;

            for (int j = 0; j < numDishes; j++) {
                StdIn.readLine(); // Read and discard the newline character after the number of ingredients
                String dishName = StdIn.readLine(); // Read dish name

                int numIngredients = StdIn.readInt(); // Read number of ingredients for this dish
                int[] ingredientIDs = new int[numIngredients];
                for (int k = 0; k < numIngredients; k++) {
                    ingredientIDs[k] = StdIn.readInt(); // Read each ingredient ID
                }

                Dish dish = new Dish(categoryVar[i], dishName, ingredientIDs);
                categoryHead = new MenuNode(dish, categoryHead); // Prepend dish to the linked list
            }

            menuVar[i] = categoryHead; // Assign the head of the linked list to the category index
        }
    }

    public MenuNode findDish ( String dishName ) {
        MenuNode menuNode = null;
        // Search all categories since we don't know which category dishName is at
        for ( int category = 0; category < menuVar.length; category++ ) {

            MenuNode ptr = menuVar[category]; // set ptr at the front (first menuNode)

            while ( ptr != null ) { // while loop that searches the LL of the category to find the itemOrdered
                if ( ptr.getDish().getDishName().equals(dishName) ) {
                    return ptr;
                } else{
                    ptr = ptr.getNextMenuNode();
                }
            }
        }
        return menuNode;
    }

    public int findCategoryIndex ( String category ) {
        int index = 0;
        for ( int i = 0; i < categoryVar.length; i++ ){
            if ( category.equalsIgnoreCase(categoryVar[i]) ) {
                index = i;
                break;
            }
        }
        return index;
    }

    /*
     * Stockroom methods
     */

    public void addStockNode(StockNode newNode) {
        // Retrieve the ingredientID from the StockNode parameter
        int ingredientID = newNode.getIngredient().getID();

        // Calculate the index using the hash function
        int index = ingredientID % stockVarSize;

        // Check if there's already a linked list at the specified index
        if (stockVar[index] == null) {
            // If there's no linked list, create a new one with the stockNode as the head
            stockVar[index] = newNode;
        } else {
            // If there's an existing linked list, add the stockNode to the front
            newNode.setNextStockNode(stockVar[index]);
            stockVar[index] = newNode;
        }
    }

    public void deleteStockNode(String ingredientName) {
        boolean found = false;
        for (int i = 0; i < stockVar.length; i++) {
            StockNode previous = null;
            StockNode current = stockVar[i];
            while (current != null) {
                if (current.getIngredient().getName().equals(ingredientName)) {
                    found = true;
                    if (previous == null) {
                        // Node to delete is the head of the list
                        stockVar[i] = current.getNextStockNode();
                    } else {
                        // Node to delete is not the head
                        previous.setNextStockNode(current.getNextStockNode());
                    }
                    break;
                }
                previous = current;
                current = current.getNextStockNode();
            }
            if (found) break;
        }
        if (!found) {
            System.out.println("Ingredient not found in stock.");
        }
    }

    public StockNode findStockNode(int ingredientID) {
        int index = ingredientID % stockVar.length;
        StockNode current = stockVar[index];
        while (current != null) {
            if (current.getIngredient().getID() == ingredientID) {
                return current;
            }
            current = current.getNextStockNode();
        }
        return null;
    }

    public StockNode findStockNode(String ingredientName) {
        for (StockNode stockNode : stockVar) {
            StockNode ptr = stockNode;
            while (ptr != null) {
                if (ptr.getIngredient().getName().equalsIgnoreCase(ingredientName)) {
                    return ptr;
                }
                ptr = ptr.getNextStockNode();
            }
        }
        return null; // Return null if no matching node is found
    }

    public void updateStock(String ingredientName, int ingredientID, int stockAmountToAdd) {
        StockNode nodeToUpdate = null;
        if (ingredientName == null && ingredientID != -1) {
            nodeToUpdate = findStockNode(ingredientID);
        } else if (ingredientID == -1 && ingredientName != null) {
            for (int i = 0; i < stockVar.length; i++) {
                StockNode current = stockVar[i];
                while (current != null) {
                    if (current.getIngredient().getName().equals(ingredientName)) {
                        nodeToUpdate = current;
                        break;
                    }
                    current = current.getNextStockNode();
                }
                if (nodeToUpdate != null) break;
            }
        }
        if (nodeToUpdate != null) {
            nodeToUpdate.getIngredient().updateStockLevel(stockAmountToAdd);
        } else {
            System.out.println("Ingredient not found.");
        }
    }

    public void updatePriceAndProfit() {
        // Assuming menuVar is an array of MenuNode
        for (int i = 0; i < menuVar.length; i++) {
            MenuNode currentNode = menuVar[i];
            while (currentNode != null) {
                Dish dish = currentNode.getDish();
                double dishCost = 0.0;
                for (int ingredientID : dish.getStockID()) {
                    StockNode ingredientNode = findStockNode(ingredientID);
                    if (ingredientNode != null) {
                        dishCost += ingredientNode.getIngredient().getCost();
                    }
                }
                double dishPrice = dishCost * 1.2;
                dish.setPriceOfDish(dishPrice);
                dish.setProfit(dishPrice - dishCost);

                currentNode = currentNode.getNextMenuNode();
            }
        }
    }

    public void createStockHashTable(String inputFile) {
        StdIn.setFile(inputFile);

        // Read the size of stockVar and update stockVarSize
        int stockVarSize = StdIn.readInt();
        stockVar = new StockNode[stockVarSize]; // Initialize the stockVar hashtable

        while (!StdIn.isEmpty()) {
            // Read ingredient details
            int ingredientID = StdIn.readInt();
            StdIn.readChar(); // To remove the space between ID and name
            String ingredientName = StdIn.readLine();
            double cost = StdIn.readDouble();
            int stockAmount = StdIn.readInt();

            // Create Ingredient and StockNode objects
            Ingredient ingredient = new Ingredient(ingredientID, ingredientName, stockAmount, cost);
            StockNode node = new StockNode(ingredient, null);

            // Add node to stockVar hashtable
            addNode(node);
        }
    }

    private void addNode(StockNode node) {
        int index = node.getIngredient().getID() % stockVar.length;
        node.setNextStockNode(stockVar[index]);
        stockVar[index] = node;
    }

    /*
     * Transaction methods
     */

    public void addTransactionNode(TransactionData data) {
        TransactionNode newNode = new TransactionNode(data, null);

        if (transactionVar == null) {
            // If the list is empty, the new node becomes the head
            transactionVar = newNode;
        } else {
            TransactionNode current = transactionVar;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
    }

    public boolean checkDishAvailability(String dishName, int numberOfDishes) {
        MenuNode menuNode = findDish(dishName);
        if (menuNode == null) {
            return false;
        }
        Dish dish = menuNode.getDish();
        int[] ingredientIDs = dish.getStockID();
        for (int ingredientID : ingredientIDs) {
            StockNode stockNode = findStockNode(ingredientID);
            if (stockNode == null || stockNode.getIngredient().getStockLevel() < numberOfDishes) {
                return false;
            }
        }
        return true;
    }

    public void order(String dishName, int quantity) {
        if (checkDishAvailability(dishName, quantity)) {
            MenuNode dishNode = findDish(dishName);
            if (dishNode != null) {
                Dish dish = dishNode.getDish();
                double totalProfit = dish.getProfit() * quantity;
                processOrder(dishName, quantity, totalProfit);
            }
        } else {
            addTransactionNode(new TransactionData("order", dishName, quantity, 0, false));

            int categoryIndex = findCategoryIndex(findDish(dishName).getDish().getCategory());
            MenuNode current = findDish(dishName);
            int dishesChecked = 0;
            int categorySize = getCategorySize(categoryIndex);

            do {
                current = current.getNextMenuNode(); // Move to the next dish in the category
                dishesChecked++;

                if (current == null) {
                    current = menuVar[categoryIndex]; // Start from the beginning of the category
                }

                if (dishesChecked >= categorySize) {
                    break; // Stop after checking all dishes in the category
                }

                if (checkDishAvailability(current.getDish().getDishName(), quantity)) {
                    Dish alternativeDish = current.getDish();
                    double profit = alternativeDish.getProfit() * quantity;
                    processOrder(alternativeDish.getDishName(), quantity, profit);
                    return;
                }

                addTransactionNode(new TransactionData("order", current.getDish().getDishName(), quantity, 0, false));

            } while (true);
        }
    }

    // Helper method to get the number of dishes in a category
    private int getCategorySize(int categoryIndex) {
        int count = 0;
        MenuNode current = menuVar[categoryIndex];
        while (current != null) {
            count++;
            current = current.getNextMenuNode();
        }
        return count;
    }

    private void processOrder(String dishName, int quantity, double profit) {
        for (int ingredientID : findDish(dishName).getDish().getStockID()) {
            updateStock(null, ingredientID, -quantity);
        }
        addTransactionNode(new TransactionData("order", dishName, quantity, profit, true));
    }

    public double profit() {
        double totalProfit = 0.0;
        TransactionNode current = transactionVar;

        while (current != null) {
            TransactionData data = current.getData();
            totalProfit += data.getProfit();
            current = current.getNext();
        }
        return totalProfit;
    }

    public void donation(String ingredientName, int quantity) {
        boolean donationSuccessful = false;
        double totalProfit = profit();

        if (totalProfit > 50) {
            StockNode stockNode = findStockNode(ingredientName);
            if (stockNode != null && stockNode.getIngredient().getStockLevel() >= quantity) {
                // Update the stock by decreasing the quantity
                updateStock(ingredientName, -1, -quantity);
                donationSuccessful = true;
            }
        }
        // Create a TransactionData object
        TransactionData donationTransaction = new TransactionData("donation", ingredientName, quantity, 0.0, donationSuccessful);
        // Add the transaction to the list, whether successful or not
        addTransactionNode(donationTransaction);
    }

    public void restock(String ingredientName, int quantity) {
        StockNode stockNode = findStockNode(ingredientName);

        if (stockNode != null) {
            double costOfRestocking = stockNode.getIngredient().getCost() * quantity;
            double totalProfit = profit();

            if (totalProfit >= costOfRestocking) {
                // profit for restocking
                updateStock(ingredientName, -1, quantity);  // Update the stock
                addTransactionNode(new TransactionData("restock", ingredientName, quantity, -costOfRestocking, true));
            } else {
                // Not enough profit for restocking
                addTransactionNode(new TransactionData("restock", ingredientName, quantity, 0, false));
            }
        } else {
            System.out.println("Ingredient not found.");
        }
    }


    /*
     * Seat guests/customers methods
     */
    public void createTables ( String inputFile ) {
        StdIn.setFile(inputFile);
        int numberOfTables = StdIn.readInt();
        tablesInfo = new int[2][numberOfTables];
        tables = new People[numberOfTables];

        for ( int t = 0; t < numberOfTables; t++ ) {
            tablesInfo[0][t] = StdIn.readInt() * StdIn.readInt();
        }
    }

    public void seatAllGuests ( Queue<People> waitingQueue ) {

        // WRITE YOUR CODE HERE
    }
}