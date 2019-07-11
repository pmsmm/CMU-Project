# CMU-Project
Mobile and Ubiquitous Computation project made in the year of 2019 for a university course.

In this project we would have to make from scratch an Application that would mimic to a certain extent Instagram. This project would consist in three components:
    -Server
    -Cloud API Integration
    -Wi-Fi Integration (Using Wi-Fi Direct)
    
The group consisted of 3 members that each took care of a particular component. For the Server we used the Javalin Framework since it would be simple to setup communication between the App and the server instead of using sockets which would be a little more time consuming. For the Cloud API we first tried going with Google Drive but later discovered that, even though all the issues we found with the documentation, lack of code examples and support from the developers, the Dropbox API would be a more simple approach that would save us a considerable ammount of time when compared to the Google Drive API.

The application itself would have to do some basic things. A user could log in to the application, create an album, add photos to that album and add users to that album. The added users would then have access to all the photos in the album and could add photos to said album so other users could see them. Albums and photos cannot be deleted but implementing this feature would not require major restructuring of our code, this is not possible because it was a requirement for the project imposed to the team by the teachers.

Server: The server, as mentioned before was implemented using the Javalin Framework in order to save time and make use of previous knowledge the group had regarding this framework. All requests to the server would then be made through HTTP as well as responses sent from the server. The server had basic functionalities, its main responsability was to manage users and albums, log in and out users, sign them up, manage users memberships to albums, keep track of a users slice in a certain album (A slice contains the links to a users photos in a certain album. These links point to a photo in the cloud so that other users may go there and download the photos to their device) and do other functions such as return all the users in the system to requesting users.

The Application: The application was made using the Android Studio IDE by Jetbrains. We made several activities and Asynchronous Tasks in order to achieve the desired results without compromising application activity. We used Java for the entire logic of the application since none of the members knew Kotlin.