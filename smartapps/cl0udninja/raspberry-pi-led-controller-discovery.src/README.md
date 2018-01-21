# Raspberry Pi LED Controller SmartApp

Installing this smartapp makes it possible to discover Raspberry Pis running the [LED Controller](https://github.com/cl0udninja/raspberrypi.ledcontroller) via UPnP.

# Installation

* Install the [LED Controller](https://github.com/cl0udninja/raspberrypi.ledcontroller) on your Pi. It's necessary to turn the LED string on and off.
* Log into the [SmartThings IDE](https://graph.api.smartthings.com)
* Click on *My SmartApps*
* Click on *Settings* and add a new GitHub repo (cl0udninja/raspberrypi.smartthings/master)
* Click on *Update from Repo* and select *raspberrypi.smartthings(master)*
* In the *New* panel on the right select *smartapps/cl0udninja/raspberry-pi-led-controller-discovery.src/raspberry-pi-led-controller-discovery.groovy* and Publish it
* Open the SmartThings app on your phone, and navigate to `Automation/SmartApps`
* Tap `+ Add a SmartApp`, select `My Apps` and from there the `Raspberry Pi LED Controller Discovery`
* Wait until your device(s) are found and tap `Select Devices` and then the `Done` in the top right corner
* For each found device a thing is added with the name `Raspberry Pi LED Switch`. You can rename the thing once it's added
* After the things are added you might need to wait a couple of seconds before they start to work

Enjoy!

# Acknowledgements

I took a look at [@nicholaswilde's berryio-smartthings](https://github.com/nicholaswilde/berryio-smartthings) based solution and since I couldn't make it work I decided to swap out the berryio backend with my own. This was my first SmartThings project and his code for the device handler was a great help!

The example on how to do UPnP discovery came from here: [@armzilla's amazon echo bridge](https://github.com/armzilla/amazon-echo-ha-bridge)
