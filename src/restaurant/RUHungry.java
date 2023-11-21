package restaurant;

import java.util.Arrays;

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
        int numCategories = StdIn.readInt();
        StdIn.readLine();
        categoryVar = new String[numCategories];
        menuVar = new MenuNode[numCategories];
        for (int i = 0; i < numCategories; i++) {
            categoryVar[i] = StdIn.readLine(); // Store the category name in categoryVar
            int numDishes = StdIn.readInt(); // gets Num of dishes in category
            MenuNode categoryHead = null;
            for (int j = 0; j < numDishes; j++) {
                StdIn.readLine();
                String dishName = StdIn.readLine();
                int numIngredients = StdIn.readInt();
                // Store ingredient IDs in an array
                int[] ingredientIDs = new int[numIngredients];
                for (int k = 0; k < numIngredients; k++) {
                    ingredientIDs[k] = StdIn.readInt();
                }
                Dish dish = new Dish(categoryVar[i], dishName, ingredientIDs); // Store ingredients in Dish
                categoryHead = new MenuNode(dish, categoryHead);
            }
            menuVar[i] = categoryHead;
            StdIn.readLine();
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

    /**
     * PICK UP LINE OF THE METHOD:
     * *can I insert myself into your life? cuz you always help me sort
     * out my problems and bring stability to my mine*
     *
     * ***********
     * This method adds a StockNode into the stockVar hashtable.
     *
     * 1. get the id of the given newNode and use a hash function to get the index at which the
     *    newNode is being inserted.
     *
     * HASH FUNCTION: id % stockVarSize
     *
     * 2. insert at the front of the linked list at the specific index
     *
     * @param newNode - StockNode that needs to be inserted into StockVar
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



    /**
     * This method deletes an ingredient (aka the specific stockNode) from stockVar.
     *
     * 1. find the node that corresponds to the ingredient (based on the ingredientName)
     *
     * 2. delete the node from stockVar
     *      a) you have to visit each index and look at each node in the corresponding linked list 
     *      b) this is NOT efficient. Hashtables are not good if you can't use the key to find the item.
     *
     * @param ingredientName - name of the ingredient
     */

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


    /**
     * This method finds an ingredient from StockVar (given the ingredientID)
     *
     * 1. find the node based upon the ingredient ID (you can go to the specific index using the hash function!)
     *      (a) this is an efficient search as it looks only at the linked list which the key hash to
     * 2. find and return the node
     *
     * @param ingredientID - the ID of the ingredient
     * @return the StockNode corresponding to the ingredientID, null otherwise
     */

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


    /**
     * This method is to find an ingredient from StockVar (given the ingredient name).
     *
     *      ** GIVEN METHOD **
     *      ** DO NOT EDIT **
     *
     * @param ingredientName - the name of the ingredient
     * @return the specific ingredient StockNode, null otherwise
     */

    public StockNode findStockNode (String ingredientName) {

        StockNode stockNode = null;

        for ( int index = 0; index < stockVar.length; index ++ ){

            StockNode ptr = stockVar[index];

            while ( ptr != null ){
                if ( ptr.getIngredient().getName().equalsIgnoreCase(ingredientName) ){
                    return ptr;
                } else {
                    ptr = ptr.getNextStockNode();
                }
            }
        }
        return stockNode;
    }

    /**
     * This method updates the stock amount of an ingredient.
     *
     * 1. you will be given the ingredientName **OR** the ingredientID:
     *      a) the ingredientName is NOT null: find the ingredient and add the given stock amount to the
     *         current stock amount
     *      b) the ingredientID is NOT -1: find the ingredient and add the given stock amount to the
     *         current stock amount
     *
     * (FOR FUTURE USE) SOMETIMES THE STOCK AMOUNT TO ADD MAY BE NEGATIVE (TO REMOVE STOCK)
     *
     * @param ingredientName - the name of the ingredient
     * @param ingredientID - the id of the ingredient
     * @param stockAmountToAdd - the amount to add to the current stock amount
     */

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

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you a single ‘for’ loop? cuz i only have i’s for you*
     *
     * ***********
     * This method goes over menuVar to update the price and profit of each dish,
     * using the stockVar hashtable to lookup for ingredient's costs.
     *
     * 1. For each dish in menuVar, add up the cost for each ingredient (found in stockVar), 
     *    and multiply the total by 1.2 to get the final price.
     *      a) update the price of each dish
     *  HINT! --> you can use the methods you've previously made!
     *
     * 2. Calculate the profit of each dish by getting the totalPrice of ingredients and subtracting from 
     *    the price of the dish itself.
     *
     */

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

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you a decimal? cuz the thought of you 
     * always floats in my head and the two of use would make double*
     *
     * ************
     * This method initializes and populates stockVar which is a hashtable where each index contains a 
     * linked list with StockNodes.
     *
     * 1. set and read the inputFile (stock.in):
     *      a) first integer (on line 1) is the size of StockVar *** update stockVarSize AND create the stockVar array ***
     *      b) first integer of next line represents the ingredientID
     *          i) example: 101 on line 2
     *      c) use StdIn.readChar() to get rid of the space between the id and the name
     *      d) the string that follows is the ingredient name (NOTE! --> there are spaces between certain strings)
     *          i) example: Lettuce
     *      e) the double on the next line corresponds to the ingredient's cost
     *          i) example: 3.12 on line 3
     *      f) the next integer is the stock amount for that ingredient
     *          i) example: 30 on line 3
     *
     * 2. create a Ingredient object followed by a StockNode then add to stockVar
     *      HINT! --> you may use previous methods written to help you!
     *
     * @param inputFile - the input file with the ingredients and all their information (stock.in)
     */

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

    /**
     * This method adds a TransactionNode to the END of the transactions linked list.
     * The front of the list is transactionVar.
     *
     * 1. create a new TransactionNode with the TransactionData paramenter.
     * 2. add the TransactionNode at the end of the linked list transactionVar.
     *
     * @param data - TransactionData node to be added to transactionVar
     */

    public void addTransactionNode(TransactionData data) {
        TransactionNode newNode = new TransactionNode(data, null);

        if (transactionVar == null) {
            // If the list is empty, the new node becomes the head
            transactionVar = newNode;
        } else {
            // Otherwise, find the last node and add the new node after it
            TransactionNode current = transactionVar;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
    }

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you the break command? cuz everything else stops when I see you*
     *
     * *************
     * This method checks if there's enough in stock to prepare a dish.
     *
     * 1. use findDish() method to find the menuNode node for dishName
     *
     * 2. retrieve the Dish, then traverse ingredient array within the Dish
     *
     * 3. return boolean based on whether you can sell the dish or not
     * HINT! --> once you determine you can't sell the dish, break and return
     *
     * @param dishName - String of dish that's being requested
     * @param numberOfDishes - int of how many of that dish is being ordered
     * @return boolean
     */

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
    /**
     * PICK UP LINE OF THE METHOD:
     * *if you were a while loop and I were a boolean, we could run 
     * together forever because I’ll always stay true to you*
     *
     * ***************
     * This method simulates a customer ordering a dish. Use the checkDishAvailability() method to check whether the dish can be ordered.
     *
     * If the dish can be prepared
     *      - create a TransactionData object of type "order" where the item is the dishName, the amount is the quantity being ordered, and profit is the dish profit multiplied by quantity.
     *      - then add the transaction as a successful transaction (call addTransactionNode()) and updates the stock accordingly.
     *
     * If the dish cannot be prepared
     *      - create a TransactionData object of type "order" where the item is the dishName, the amount is the quantity being ordered, and profit is 0 (zero).
     *      - then add the transaction as an UNsuccessful transaction and,
     *      - simulate the customer trying to order other dishes in the same category linked list:
     *          - if the dish that comes right after the dishName can be prepared, great. If not, try the next one and so on.
     *          - you might have to traverse through the entire category searching for a dish that can be prepared. If you reach the end of the list, start from the beginning until you have visited EVERY dish in the category.
     *          - It is possible that no dish in the entire category can be prepared.
     *          - Note: the next dish the customer chooses is always the one that comes right after the one that could not be prepared. 
     *
     *
     * @param dishName - String of dish that's been ordered
     * @param quantity - int of how many of that dish has been ordered
     */
    public void order(String dishName, int quantity) {
        MenuNode dishNode = findDish(dishName);
        boolean isOrderSuccessful = checkDishAvailability(dishName, quantity);

        if (isOrderSuccessful) {
            // Dish can be prepared
            processOrder(dishNode.getDish(), quantity, true);
        } else {
            // Dish cannot be prepared, add unsuccessful transaction for this dish
            processOrder(dishNode.getDish(), quantity, false);

            // Try finding an alternative dish in the same category
            MenuNode categoryStartNode = findCategoryStartNode(dishName);
            if (categoryStartNode != null) {
                // Start from the next node after the current dish
                MenuNode current = categoryStartNode;
                boolean foundAlternative = false;
                do {
                    current = current.getNextMenuNode();
                    if (current == null) {
                        // If reached the end, loop back to the start of the category
                        current = categoryStartNode;
                    }

                    if (current.getDish().getDishName().equals(dishName)) {
                        // If we have looped through all dishes, break the loop
                        break;
                    }

                    if (checkDishAvailability(current.getDish().getDishName(), quantity)) {
                        // Found an alternative dish that can be prepared
                        processOrder(current.getDish(), quantity, true);
                        foundAlternative = true;
                        break;
                    }

                } while (!foundAlternative);
            }
        }
    }

    private void processOrder(Dish dish, int quantity, boolean isSuccess) {
        double profit = isSuccess ? dish.getProfit() * quantity : 0;
        if (isSuccess) {
            // Update stock for each ingredient
            for (int ingredientID : dish.getStockID()) {
                updateStock(null, ingredientID, -quantity);
            }
        }
        // Create and add transaction data
        TransactionData transaction = new TransactionData("order", dish.getDishName(), quantity, profit, isSuccess);
        addTransactionNode(transaction);
    }

    private MenuNode findCategoryStartNode(String dishName) {
        // Use findDish to get the dish's category
        MenuNode dishNode = findDish(dishName);
        if (dishNode == null) {
            // Dish not found
            return null;
        }

        String category = dishNode.getDish().getCategory();
        // Use findCategoryIndex to find the index of this category
        int categoryIndex = findCategoryIndex(category);

        // Return the start node of this category from menuVar
        return menuVar[categoryIndex];
    }


    /**
     * This method returns the total profit for the day
     *
     * The profit is computed by traversing the transaction linked list (transactionVar) 
     * adding up all the profits for the day
     *
     * @return profit - double value of the total profit for the day
     */

    public double profit() {
        double totalProfit = 0.0;
        TransactionNode current = transactionVar;

        while (current != null) {
            TransactionData data = current.getData();
            if (data != null && data.getSuccess()) { // Only count profits from successful transactions
                totalProfit += data.getProfit();
            }
            current = current.getNext();
        }

        return totalProfit;
    }


    /**
     * This method simulates donation requests, successful or not.
     *
     * 1. check whether the profit is > 50 and whether there's enough ingredients in stock.
     *
     * 2. add transaction to transactionVar
     *
     * @param ingredientName - String of ingredient that's been requested
     * @param quantity - int of how many of that ingredient has been ordered
     * @return void
     */

    public void donation ( String ingredientName, int quantity ){

        // WRITE YOUR CODE HERE
    }

    /**
     * This method simulates restock orders
     *
     * 1. check whether the profit is sufficient to pay for the total cost of ingredient
     *      a) (how much each ingredient costs) * (quantity)
     *      b) if there is enough profit, adjust stock and profit accordingly
     *
     * 2. add transaction to transactionVar
     *
     * @param ingredientName - ingredient that's been requested
     * @param quantity - how many of that ingredient needs to be ordered
     */

    public void restock ( String ingredientName, int quantity ){

        // WRITE YOUR CODE HERE
    }

    /*
     * Seat guests/customers methods
     */

    /**
     * Method to populate tables (which is a 2d integer array) based upon input file
     *
     * First row of tables[][]: contains the total number of seats available at a table (each table is an index in first row)
     * Second row of tables[][]: initializes all indices to 0
     *      0 --> table is available
     *      1 --> table is occupied
     *
     *          ** GIVEN METHOD **
     *          ** DO NOT EDIT **
     *
     * @param inputFile - tables1.in (contains all the tables in the RUHungry restaurant)
     * @return void (aka nothing)
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

    /**
     * PICK UP LINE OF THE METHOD:
     * *are you a linked list? cuz nothing could stock up to you and 
     * you’re pretty queue(te)*
     *
     * ***************
     * This method simulates seating guests at tables. You are guaranteed to be able to sit everyone from the waitingQueue eventually.
     *
     * 1. initialize leftQueueVar a People queue that represents the people that have left the restaurant
     *
     * 2. while there are people waiting to be sat:
     *      - Starting from index 0 (zero), seat the next party in the first available table that fits their party.
     *      - If there is no available table for the next party, kick a party out from the tables array:
     *          1. starting at index 0 (zero), find the first table big enough to hold the next party in line.
     *          2. remove the current party, add them to the leftQueueVar.
     *          3. seat the next party in line.
     *
     * tableInfo contains the number of seats per table as well as if the table is occupied or not.
     * tables contains the People object (party) currently at the table.
     *
     * Note: After everyone has been seated (waitingQueue is empty), remove all the parties from tables and add then to the leftQueueVar.
     *
     * @param waitingQueue - queue containing people waiting to be seated (each element in queue is a People <-- you are given this class!)
     */

    public void seatAllGuests ( Queue<People> waitingQueue ) {

        // WRITE YOUR CODE HERE
    }
}