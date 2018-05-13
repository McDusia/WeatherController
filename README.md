# WeatherController

This program read actual weather data from the Internet using Weather API - OpenWeatherMap (data: temperature, pressure, humidity, text description) for Kraków and send it to kaa server.
Server should be palaced on a virtual machine. 
Appropriate setting in kaa server allows to gather and keep data in MongoDB and to change sending period in runtime. 
It could be lots of clients which send data.
