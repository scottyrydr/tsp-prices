# tsp-prices

Retrieve Thrift Savings Plan Fund prices and save as CSV files.

The Thrift Savings Plan (TSP) does not interface directly with financial management software, such as Quicken. This
program downloads
a 30 day history of prices for all TSP funds, and writes them to individual CSV files that can be imported into Quicken.

This software does not have access to any private information, it
only accesses share price history information that is publicly available on the TSP
website. This software neither requires nor requests authentication with the TSP website.

This program can be used in one of two modalities, 1) direct URL access and 2) transforming a CSV file that
has been manually downloaded from the TSP website.

## Direct URL Access

Execute the program with no command line arguments. The program will direct your local instance of Chrome
to access the public TSP share price history URL, retrieve year-to-date price history,
and transform that info into CSV files that can be imported into Quicken. This modality does require that a recent
version of Chrome For Testing is installed. Testing was done using Chrome For Testing Version 154.0.8037.57 (Official
Build) (arm64).

In addition to saving individual CSV files for each fund type, this program will also write a CSV file containing a
price
history of all TSP funds. Quicken (for Mac) can then load a complete fund price history for all TSP funds as a single
file.
NOTE - That single file, TSP-All-Funds.csv, uses custom names for the funds since TSP fund prices are not available
through
Quicken's automated price download feature.

## Manual Download CSV Transformation

You must first navigate to the TSP website using a browser of your choice, manually retrieve the desired share price
history
and click the appropriate button to download that as a CSV from the TSP website.
Then you execute the tsp-prices program with the "-f <filename.csv>" option (with the correct
name of the downloaded CSV file). The program will transform the information
in the CSV file into one or more CSV files in the correct format for Quicken.
