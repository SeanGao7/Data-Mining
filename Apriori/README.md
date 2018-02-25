# Apriori Algorithm Implementation in Java
#	Compilation and usage
    o	Java SDK 1.8 required as predicate is used to improve performance
    o	To compile: javc Apriori.java
    o	To run: java Apriori [path to data file] [minimal support threshold] [path to output]
#	Result
    o	Result is intentionally unsorted to save the complexity in sorting and make the container data structure for result faster (HashMap<List<Integer>> to List<String>)
    o	Test environment:
        	Windows 10
        	Intel i7-6700HQ
        	16 GB DDR3
#	Tested on two datasets:
        	T10I4D100K (500) 	2.3s ~ 2.6s
        	Chess (2000)		329s
#	Optimization made
    o	Prune the items in database does not belong in the list of items from candidates
    o	Prune the entire line if the line did not generate a subset belong to candidates
    o	Made use of heuristic to choose between:a
        	 a Brute Force Algorithm which traverse each candidate / transaction to count their frequency (validateWithSet in code)
        	Another Algorithm generate subsets of size k for a specific transaction and update the candidate HashMap. (validateWithList in code)
    o	Brute Force works better when the number in each transaction is large
    o	Subset Generation works better when the candidate set is large

