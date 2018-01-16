# Raspberry Pi Monitor

For the application to run on your Raspberry Pi click [here](https://github.com/cl0udninja/raspberrypi.monitor)

# Overview

The device handler is capable of displaying a couple of information about your Raspberry Pi, such as the

* CPU temperature, speed and core voltage
* Free memory
* Some meta information, like Java version, board type, hostname

<center><img src=".smartthings.screenshot.jpg" width="300"></center>

# Installation

* Install the monitor on your Pi. It's necessary to get the information to show in the device handler. See link above
* Log into the [SmartThings IDE](https://graph.api.smartthings.com)
* Click on *My Device Handlers*
* Click on *Settings* and add a new GitHub repo (cl0udninja/raspberrypi.smartthings/master)
* Click on *Update from Repo* and select *raspberrypi.smartthings(master)*
* In the *New* panel on the right select *devicetypes/cl0udninja/raspberry-pi-monitor.src/raspberry-pi-monitor.groovy* and Publish it
* Click on the new Device Handler and click on the *Publish* button and select *For Me*
* Click on *My Devices* and click on *New Device*
* Enter the Name you want, and enter anything in the *Device Network Id* you want. It will be overwritten with the Hex value of your Pi's IP address and your monitor's port.
* For type select *Raspberry Pi Monitor*
* Select the Location and Hub and click *Create*
* Once created click on it in the *Device List* and edit the *Preferences* (see below). You can do the same in the SmartThings app as well. Enter the Pi's IP address and the monitor apps's port (default is 8080)

<center><img src=".smartthings.device.configure.png"></center>

You should be all set and be able to see the information about your Pi.

# Acknowledgements

I took a look at [@nicholaswilde's berryio-smartthings](https://github.com/nicholaswilde/berryio-smartthings) based solution and since I couldn't make it work I decided to swap out the berryio backend with my own. This was my first SmartThings project and his code for the device handler was a great help!