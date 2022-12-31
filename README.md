# tsp-prices

Retrieve Thrift Savings Plan Fund prices and save as CSV files.

The Thrift Savings Plan (TSP) does not interface directly with financial management software, such as Quicken. This
program downloads
a 30 day history of prices for all TSP funds, and writes them to individual CSV files that can be imported into Quicken.

This software does not have access to any private information, it
only accesses share price history information that is publicly availabe on the TSP
website. This software neither requires nor requests authentication with the TSP website.

This program can be used in one of two different modalities, 1) direct URL access and 2) transforming a CSV file that
has been manually
downloaded from the TSP website.

Direct URL Access
Execute the program with no command line arguments. The program will direct your local instance of Chrome
to access the TSP share price history URL, retrieve the previous 30 days of price history,
and transform that info into CSV files that can be imported into Quicken.

Manual Download CSV Transformation
You must first navigate to the TSP website, manually retrieve the desired share price history
and click the appropriate button to download that as a CSV from the TSP website.
Then you execute the tsp-prices program with the "-f <filename.csv>" command (with the correct
name of the downloaded CSV file). The program will transform the information
in the CSV file into one or more CSV files in the correct format for Quicken.
