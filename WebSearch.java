package aiassignment04;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;


// You should call this code as follows:
// java WebSearch directoryName searchStrategyName (or jview, in J++)
//
// where <directoryName> is the name of corresponding intranet folder (internet1, internet5, or internet7)
// and <searchStrategyName> is one of {breadth, depth, best, beam}.
// ex: java WebSearch intranet1 breadth

// The PARTIAL code below contains code for FETCHING and PARSING
// the simple web pages we're using, as well as the fragments of
// a solution.  BE SURE TO READ ALL THE COMMENTS.

// the only requirement is that your main class be called WebSearch
// and that it accept the two arguments described above.

public class WebSearch 
{
	static LinkedList<SearchNode> OPEN; // Feel free to choose your own data structures for searching,
	static HashSet<String> CLOSED;      // and be sure to read documentation about them.
        static LinkedList<SearchNode> Visited;
        static LinkedList<SearchNode> BeamSet;
        static int counter = 0;
        
	static final boolean DEBUGGING = false; // When set to TRUE, the program will report what's happening.
                                                // WARNING: lots of info is printed.

	static int beamWidth = 10; // If searchStrategy = "beam",
	// limit the size of OPEN to this value.
	// The setSize() method in the Vector
	// class can be used to accomplish this.

	static final String START_NODE     = "https://stackoverflow.com";  // The starting file of the search.

	// A web page is a goal node if it includes the following string.        
	static final String GOAL_PATTERN   = "blabla";

	public static void main(String args[])
	{ 
		if (args.length != 1)
		{
			System.out.println("You must provide the directoryName and searchStrategyName.  Please try again.");
		}
		else
		{
			String searchStrategyName = args[0]; // Read the search strategy to use (put the search algorithm name in searchStrategyName).

			if (searchStrategyName.equalsIgnoreCase("breadth") ||
                            searchStrategyName.equalsIgnoreCase("depth")   ||
                            searchStrategyName.equalsIgnoreCase("best")    ||
                            searchStrategyName.equalsIgnoreCase("beam"))
			{
				performSearch(START_NODE, searchStrategyName);
			}
			else
			{
				System.out.println("The valid search strategies are:");
				System.out.println(" BREADTH DEPTH BEST BEAM");
			}
		}

		Utilities.waitHere("Press ENTER to exit.");
	}

	static void performSearch(String startNode, String searchStrategy)
	{
		int nodesVisited = 0;

		OPEN   = new LinkedList<SearchNode>();
		CLOSED = new HashSet<String>();
                Visited = new LinkedList<SearchNode>();
                BeamSet = new LinkedList<SearchNode>();
                String contents = "";
                
                SearchNode child = new SearchNode(startNode);
                OPEN.add(child); //Adding the startNode to the OPEN LinkedList.
                
                
		while (!OPEN.isEmpty())
		{
			
                        if (searchStrategy.equalsIgnoreCase("beam"))
                            beamWidth--;                        
                        
                        SearchNode currentNode = pop(OPEN,searchStrategy);
                        System.out.println("The poped Child Node is : " + currentNode.getNodeName());
			String currentURL = currentNode.getNodeName();

			nodesVisited++;

                    try {
                        // Go and fetch the contents of this file.
                        contents = getWebPabeSource(currentURL);
                        //= Utilities.getFileContents(directoryName + File.separator + currentURL); // Ex: the input could be (Internet1/file1)
                    } catch (IOException ex) {
                        Logger.getLogger(WebSearch.class.getName()).log(Level.SEVERE, null, ex);
                    }

			if (isaGoalNode(contents))
			{
                            //++++++++ HERE I HAVE TO WRITE THE CODE THAT WILL PRINT THE PATH FROM THE SOLUTION FILE TO THE START.
                            
                            // Report the solution path found (You might also wish to write a method that
                            // counts the solution-path's length, and then print that number here.)
                            System.out.println("The Solution has been found \nThe Solution node is : " + currentURL); 
                            //Iterator<String> itr = new CLOSED.Iterator();                            
                            //currentNode.reportSolutionPath();
                            
                            currentNode.reportSolutionPath(currentNode);
                            break;
			}

			// Remember this node was visited.
			CLOSED.add(currentURL);
                        Visited.add(new SearchNode(currentURL)); //Adding the startNode to the Visited LinkedList.


                        counter=0;
			addNewChildrenToOPEN(currentNode, contents, searchStrategy);

			// Provide a status report.
			if (DEBUGGING) System.out.println("Nodes visited = " + nodesVisited + " |OPEN| = " + OPEN.size());
		}

		System.out.println(" Visited " + nodesVisited + " nodes, starting @" +
				" " + startNode + ", using: " + searchStrategy + " search.");
	}

	// This method reads the page's contents and collects the 'children' nodes (ie, the hyperlinks on this page).
	// The parent node is also passed in so that 'backpointers' can be created (in order to later extract solution paths).
	static void addNewChildrenToOPEN(SearchNode parent, String contents, String searchStrategy)
	{
		// StringTokenizer's are a nice class built into Java.
		// Be sure to read about them in some Java documentation.
		// They are useful when one wants to break up a string into words (tokens).
		StringTokenizer st = new StringTokenizer(contents);

		while (st.hasMoreTokens())
		{
                    
			String token = st.nextToken();

			// Look for the hyperlinks on the current page.

			// (Lots a print statments and error checks are in here, both as a form of documentation
                        // and as a debugging tool should you create your own intranets.)

			// At the start of some hypertext?  Otherwise, ignore this token.
			if (token.startsWith("href=") && counter <10)
			{
                       
				String hyperlink; // The name of the child node.

				if (DEBUGGING) System.out.println("Encountered a HYPERLINK");
                                                                
				hyperlink = token.substring(6);
                                hyperlink = hyperlink.substring(0, hyperlink.indexOf("\""));
                                
                                // if the hyperlink start with http then its fine, if not we have to add the current webaddress before it.
                                if (hyperlink.startsWith("http"))
                                {   
                                    System.out.println("Complete hyperlink for the child is : " + hyperlink);
                                
				////////////////////////////////////////////////////////////////////////
				// Have collected a child node; now have to decide what to do with it.//
				////////////////////////////////////////////////////////////////////////
                                
				if (alreadyInOpen(hyperlink))
				{   // If already in OPEN, we'll ignore this hyperlink
                                    // (Be sure to read the "Technical Note" below.)
                                    if (DEBUGGING) System.out.println(" - this node is in the OPEN list.");
				}
				else if (CLOSED.contains(hyperlink))
				{   // If already in CLOSED, we'll also ignore this hyperlink.
                                    if (DEBUGGING) System.out.println(" - this node is in the CLOSED list.");
				}
                                else if (alreadyInBeam(hyperlink) && searchStrategy.equalsIgnoreCase("beam"))
				{   // If already in CLOSED, we'll also ignore this hyperlink.
                                    if (DEBUGGING) System.out.println(" - this node is in the BeamSet list.");
				}
				else 
				{ 
                                        // Collect the hypertext if this is a previously unvisited node.
					// (This is only needed for HEURISTIC SEARCH, but collect in
					// all cases for simplicity.)
					

					//////////////////////////////////////////////////////////////////////
					// At this point, you have a new child (hyperlink) and you have to  //
					// insert it into OPEN according to the search strategy being used. //
					// Your heuristic function for best-first search should accept as   //
					// arguments both "hypertext" (ie, the text associated with this    //
					// hyperlink) and "contents" (ie, the full text of the current page)./
					//////////////////////////////////////////////////////////////////////

					// Technical note: in best-first search,
					// if a page contains TWO (or more) links to the SAME page,
					// it is acceptable if only the FIRST one is inserted into OPEN,
					// rather than the better-scoring one.  For simplicity, once a node
					// has been placed in OPEN or CLOSED, we won't worry about the
					// possibility of later finding of higher score for it.
					// Since we are scoring the hypertext POINTING to a page,
					// rather than the web page itself, we are likely to get
					// different scores for given web page.  Ideally, we'd
					// take this into account when sorting OPEN, but you are
					// NOT required to do so (though you certainly are welcome
					// to handle this issue).

					// HINT: read about the insertElementAt() and addElement()
					// methods in the Vector class.
                                        
                                        //Moh'd
                                      counter ++;
                                        SearchNode ChildNode = new SearchNode(hyperlink);
                                        ChildNode.setNodeParent(parent);
                                        
                                        ChildNode.sethValue(ChildNode.CalculateHValue(hyperlink, contents));
                                        //System.out.println("the weight of the link is :" + ChildNode.CalculateHValue(hypertext, contents));
 
                                        if (searchStrategy.equalsIgnoreCase("beam"))
                                            BeamSet.add(ChildNode);
                                        else
                                            OPEN.add(ChildNode); //Adding the new child to the OPEN LinkedList.
				}
                                }
			}
		}
                
                if (beamWidth<=0 || OPEN.isEmpty())
                {                                        
                    if (!OPEN.isEmpty())
                        for (int i=0; i<OPEN.size();i++)
                            CLOSED.add(OPEN.get(i).getNodeName());  
                    OPEN.clear();
                    beamWidth=10;
                }
                                                        
                if (OPEN.isEmpty() && searchStrategy.equalsIgnoreCase("beam"))                                        
                {                                            
                    for (int i=0; i<BeamSet.size();i++)                                               
                        OPEN.add(BeamSet.get(i));                                           
                    BeamSet.clear();                              
                }
	}

	// A GOAL is a page that contains the goalPattern set above.
	static boolean isaGoalNode(String contents)
	{
            return (contents != null && contents.indexOf(GOAL_PATTERN) >= 0);
	}

	// Is this hyperlink already in the OPEN list?
	// This isn't a very efficient way to do a lookup, but its fast enough for this homework.
	// Also, this for-loop structure can be adapted for use when inserting nodes into OPEN according to their heuristic score.
	static boolean alreadyInOpen(String hyperlink)
	{
		int length = OPEN.size();

		for(int i = 0; i < length; i++)
		{
			SearchNode node = OPEN.get(i);
			String oldHyperlink = node.getNodeName();

			if (hyperlink.equalsIgnoreCase(oldHyperlink)) return true;  // Found it.
		}
		return false;  // Not in OPEN.    
	}
        
        	static boolean alreadyInBeam(String hyperlink)
	{
		int length = BeamSet.size();

		for(int i = 0; i < length; i++)
		{
			SearchNode node = BeamSet.get(i);
			String oldHyperlink = node.getNodeName();

			if (hyperlink.equalsIgnoreCase(oldHyperlink)) return true;  // Found it.
		}
		return false;  // Not in OPEN.    
	}

	// You can use this to remove the first element from OPEN.
	static SearchNode pop(LinkedList<SearchNode> list, String searchStrategy)
	{
            //Moh'd
            SearchNode result;
            if (searchStrategy.equalsIgnoreCase("breadth"))
                result = list.removeFirst();
            else if (searchStrategy.equalsIgnoreCase("depth"))
                result = list.removeLast();
            else if (searchStrategy.equalsIgnoreCase("best"))
            {
                BubbleSort(list);
                result = list.removeFirst();                
                
//                // Print the Bubbe Sorted List, so you make sure it is sorted in none decreasing order.
//                int len = list.size();
//                System.out.println("The Sorted child nodes based on their h values are: ");
//                for (int i=0; i < len; i++)
//                {
//                    SearchNode p = list.get(i);
//                    System.out.print(p.getNodeName() + " , ");         
//                }
//                System.out.println("");                
            }
            else
            {
                BubbleSort(list);
                result = list.removeFirst(); // this one need to by modify later for the other two kinds of search
            }
		
            return result;
	}
        
        public static void BubbleSort(LinkedList<SearchNode> objs)
        {
            int len = objs.size();
            for(int pass = 1; pass < len; pass++) 
            {
                for (int i=0; i < len - pass; i++) {
                    if(objs.get(i).getNodehValue() > objs.get(i + 1).getNodehValue()) 
                    {
                        SearchNode p = objs.get(i);
                        objs.set(i,objs.get(i+1));
                        objs.set(i + 1, p);
                    }
                }
            }
        }
        
        
        private static String getWebPabeSource(String sURL) throws IOException 
        {
            URL url = new URL(sURL);        
            URLConnection urlCon = url.openConnection();        
            BufferedReader in = null;
            if (urlCon.getHeaderField("Content-Encoding") != null && urlCon.getHeaderField("Content-Encoding").equals("gzip")) 
            {           
                in = new BufferedReader(new InputStreamReader(new GZIPInputStream(urlCon.getInputStream())));
            } 
            else            
                in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));

            String inputLine;        
            StringBuilder sb = new StringBuilder();
        
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            
            return sb.toString();
        }
}

/////////////////////////////////////////////////////////////////////////////////

// You'll need to design a Search node data structure.

// Note that the above code assumes there is a method called getHvalue()
// that returns (as a double) the heuristic value associated with a search node.
// a method called getNodeName() that returns (as a String)
// the name of the file (eg, "page7.html") associated with this node.
// a (void) method called reportSolutionPath() that prints the path
// from the start node to the current node represented by the SearchNode instance.
class SearchNode
{
	final String nodeName;
        private SearchNode nodeParent;
        private double hValue;
        private String hyperLink;
        
	public SearchNode(String name) 
        {
		nodeName = name;
        }
        
        public double CalculateHValue(String hypertext, String containts)
        {
            double Value = 0;
            int WordsCount = 0;
            
            // 1. The more QUERY words on a page, the more likely the links on that page lead to the goal node.     
            StringTokenizer Allst = new StringTokenizer(containts);            
            while (Allst.hasMoreTokens())
            {
                WordsCount++;
                String token = Allst.nextToken();
                if (token.equalsIgnoreCase("computer")||token.equalsIgnoreCase("science")||token.equalsIgnoreCase("master")||token.equalsIgnoreCase("PhD")||token.equalsIgnoreCase("graduate")||token.equalsIgnoreCase("information technology")||token.equalsIgnoreCase("student")||token.equalsIgnoreCase("people")||token.equalsIgnoreCase("mohammed")||token.equalsIgnoreCase("khaleel"))
                {
                    // If the word Query position at the begining of the documents (first 20% of the documents) the link will get more weight.
                    if (WordsCount<=(containts.length()/5))
                            Value++;
                    Value++;
                } 
                    
            }
            
            // 2. The more QUERY words in the hypertext associated with a hyperlink,
            //    the more likely that hyperlink leads to the goal node.        
            StringTokenizer st = new StringTokenizer(hypertext);            
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                if (token.equalsIgnoreCase("computer")||token.equalsIgnoreCase("science")||token.equalsIgnoreCase("master")||token.equalsIgnoreCase("PhD")||token.equalsIgnoreCase("graduate")||token.equalsIgnoreCase("information technology")||token.equalsIgnoreCase("student")||token.equalsIgnoreCase("people")||token.equalsIgnoreCase("mohammed")||token.equalsIgnoreCase("khaleel"))
                    Value+=2;
            }
            
            // 3. The more consecutive and in numerical order QUERY words there are in a hyperlink,
            //    the more likely that hyperlink leads to the goal node. 
            //    e.g. seeing QUERY1 QUERY2 QUERY3 is a very good indicator
            if (hypertext.contains("Department") && hypertext.contains("Computer") && hypertext.contains("Science"))
                Value+=12;
            else if ((hypertext.contains("QUERY1") && hypertext.contains("QUERY2") && hypertext.contains("QUERY3")) || (hypertext.contains("QUERY2") && hypertext.contains("Query3") && hypertext.contains("QUERY4")))
                Value+=10;
            else if ((hypertext.contains("QUERY1") && hypertext.contains("QUERY2") && hypertext.contains("QUERY4")) || (hypertext.contains("QUERY1") && hypertext.contains("Query3") && hypertext.contains("QUERY4")))
                Value+=8;
            else if ((hypertext.contains("QUERY1") && hypertext.contains("QUERY2")) || (hypertext.contains("QUERY2") && hypertext.contains("QUERY3")) || (hypertext.contains("Query3") && hypertext.contains("QUERY4")))
                Value+=6;
            else if ((hypertext.contains("QUERY1") && hypertext.contains("QUERY3")) || (hypertext.contains("QUERY2") && hypertext.contains("QUERY4")))
                Value+=4;
            else if (hypertext.contains("QUERY1") && hypertext.contains("QUERY4"))
                Value+=2;

            // Return the Value as a huristic function (The more weight Value will have the best node will be to choose).
            return (1-(Value/100));
            
        }
        
        public void setHyperText(String t)
        {
            hyperLink = t;
        }
        
               
        public String getHyperText()
        {
            return hyperLink;
        }
        
        public void sethValue(double value)
        {
            hValue = value;
        }

        public void setNodeParent(SearchNode node)
        {
            nodeParent = node;
        }
        
        public static void reportSolutionPath(SearchNode currentNode) 
        {
            /////// we have to write the functin that reutrns the current path.
            if (currentNode.getNodeParentName()== null)
            {
                System.out.println(currentNode.getNodeName()+ " [" + currentNode.getHyperText() + " ] " + "-->");
                return;
            }
            else
            {
                reportSolutionPath(currentNode.getNodeParentName());
                System.out.println(currentNode.getNodeName()+ " [" + currentNode.getHyperText() + "] " + "-->");  
                return;                
            }
        }        
       
	public SearchNode getNodeParentName() 
        {
		return nodeParent;                
	} 
	
        public String getNodeName() 
        {
            return nodeName;
	} 
  
        public double getNodehValue() 
        {
            return hValue;
	} 
}

/////////////////////////////////////////////////////////////////////////////////

// Some 'helper' functions follow.  You don't need to understand their internal details.
// Feel free to move this to a separate Java file if you wish.
class Utilities
{
	// In J++, the console window can close up before you read it,
	// so this method can be used to wait until you're ready to proceed.
	public static void waitHere(String msg)
	{
		System.out.println("");
		System.out.println(msg);
		try { System.in.read(); } catch(Exception e) {} // Ignore any errors while reading.
	}

	// This method will read the contents of a file, returning it
	// as a string.  (Don't worry if you don't understand how it works.)
	public static synchronized String getFileContents(String fileName)
	{
		File file = new File(fileName);
		String results = null;

		try
		{
			int length = (int)file.length(), bytesRead;
			byte byteArray[] = new byte[length];

			ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(length);
			FileInputStream       inputStream = new FileInputStream(file);
			bytesRead = inputStream.read(byteArray);
			bytesBuffer.write(byteArray, 0, bytesRead);
			inputStream.close();

			results = bytesBuffer.toString();
		}
		catch(IOException e)
		{
			System.out.println("Exception in getFileContents(" + fileName + "), msg=" + e);
		}

		return results;
	}
}               
