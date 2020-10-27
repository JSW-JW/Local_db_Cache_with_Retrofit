# Local-db-Cache-Retrofit-REST-API-MVVM
App that interacts with a REST API using Retrofit. There is a local db cache and architecture is MVVM.
<br><br>

# Single Source of Truth Principal

* In case of this app, the view gets updated from datas which are retrieved from local db cache.
* Local db cache gets updated from the Rest Api.
* And MVVM pattern is proper for making this kind of App flow.
* This kind of App flow(view being updated from the local db cache only) makes faster UX.

# why convert retrofit call object to LiveData?

* We don't need to use executors to make it executed on background thread. Because livedata is automatically executed asynchronously.
* This makes the code more concise. 

# biggest difference from the previous app.

* Retrieving the data from network no more needs to be done on background thread because it is converted to LiveData.
* Instead, cause it is using room database, retrieving cache from the local db is done on background thread. Room Library process is always executed on background.


