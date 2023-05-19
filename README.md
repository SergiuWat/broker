# Broker

## Endpoints
- search:
  - Example: http://address:port/search?q=<query>, where query is the word or phrase to search in the databases with.
  
- get_snippet:
  - Example: http://address:port/get_snippet?databaseName=<name>&databaseTable=<name>, where databaseName and databaseTable are the names of the database and table respectivly where the snippet should come from
  
- get_dataset:
  - Example: http://address:port/get_dataset?databaseName=<name>&databaseTable=<name>&format=<type>, where databaseName and databaseTable are the names of the database and table respectivly where the dataset should come from and the format specify how the dataset should came back
- get_all_meta
  - Example: http://address:port/get_all_meta
- insert_tokens
- get_tokens
