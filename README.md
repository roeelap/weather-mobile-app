# Welcome to our TeamWeather app!

It's the final project for our mobile development course.
The app has a login page, a map, a weather page, and a server.
We used Java for the frontend, Node.js for the backend server, and mongoDB (for the data base).

The login - Sign in page, new users can create a new account.

The map - The user can pick a place and save it in any name, it is also possible to choose from the past places he saved.
After saving a place the user can press the get weather button to continue to the weather page.

The weather page - In case of no date or a date that has no forcast, we write a message to inform the user.
In case the date entered has a forcast, we parse the Json received and insert the data to the weather table.
We also added a nice icon that describes best the weather of the chosen date.

The server - 
1. Getting the weather from the free service at openweathermap.org.
2. Creating and checking if users exists in our mongo data base.
In order to deal better with passwards we used Bycrpt library to hash the passwords.
3. Saving for each user thier selected places.