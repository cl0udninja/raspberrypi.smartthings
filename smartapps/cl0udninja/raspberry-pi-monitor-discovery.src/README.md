# Raspberry Pi Monitor SmartApp

Installing this smartapp makes it possible to discover Raspberry Pis running the [monitor](https://github.com/cl0udninja/raspberrypi.monitor) via UPnP.

# Installation

* Install the monitor on your Pi. It's necessary to get the information to show in the device handler.
* Log into the [SmartThings IDE](https://graph.api.smartthings.com)
* Click on *My SmartApps*
* Click on *Settings* and add a new GitHub repo (cl0udninja/raspberrypi.smartthings/master)
* Click on *Update from Repo* and select *raspberrypi.smartthings(master)*
* In the *New* panel on the right select *smartapps/cl0udninja/raspberry-pi-monitor-discovery.src/raspberry-pi-monitor-discovery.groovy* and Publish it
* Open the SmartThings app on your phone, and navigate to `Automation/SmartApps`
* Tap `+ Add a SmartApp`, select `My Apps` and from there the `Raspberry Pi Monitor Discovery`
* Wait until your device(s) are found and tap `Select Devices` and then the `Done` in the top right corner
* For each found device a thing is added with the name `Raspberry Pi Monitor`. You can see the hostname after the information was pulled from the Pi and rename your device.
* After the things are added you might need to wait a couple of seconds before they start to work

Enjoy!

# Acknowledgements

I took a look at [@nicholaswilde's berryio-smartthings](https://github.com/nicholaswilde/berryio-smartthings) based solution and since I couldn't make it work I decided to swap out the berryio backend with my own. This was my first SmartThings project and his code for the device handler was a great help!

The example on how to do UPnP discovery came from here: [@armzilla's amazon echo bridge](https://github.com/armzilla/amazon-echo-ha-bridge)
