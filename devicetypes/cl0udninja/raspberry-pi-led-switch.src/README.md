# Raspberry Pi LED Controller Device Handler

For the application to run on your Raspberry Pi click [here](https://github.com/cl0udninja/raspberrypi.ledcontroller)

# Overview

This device handler is capable turning on and off the LED string attached to your RPi. See the link above for further details on setting up your RPi.

# Installation

* Install the [LED Controller](https://github.com/cl0udninja/raspberrypi.ledcontroller) on your Pi.
* Log into the [SmartThings IDE](https://graph.api.smartthings.com)
* Click on *My Device Handlers*
* Click on *Settings* and add a new GitHub repo (cl0udninja/raspberrypi.smartthings/master)
* Click on *Update from Repo* and select *raspberrypi.smartthings(master)*
* In the *New* panel on the right select *devicetypes/cl0udninja/raspberry-pi-led-switch.src/raspberry-pi-led-switch.groovy* and Publish it
* Install the SmartApp for UPnP discovery in the IDE. Follow the steps [here](..\..\..\smartapps\cl0udninja\raspberry-pi-led-controller-discovery.src\README.md)

# Acknowledgements

I took a look at [@nicholaswilde's berryio-smartthings](https://github.com/nicholaswilde/berryio-smartthings) based solution and since I couldn't make it work I decided to swap out the berryio backend with my own. This was my first SmartThings project and his code for the device handler was a great help!

The example on how to do UPnP discovery came from here: [@armzilla's amazon echo bridge](https://github.com/armzilla/amazon-echo-ha-bridge)
