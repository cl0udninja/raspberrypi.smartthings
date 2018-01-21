/**
 *  Generic UPnP Service Manager
 *
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
		name: "Raspberry Pi Monitor Discovery",
		namespace: "cl0udninja",
		author: "Janos Elohazi",
		description: "Raspberry Pi Monitor convenient device discovery",
		category: "SmartThings Labs",
		iconUrl: "http://storage.googleapis.com/storage.cl0ud.ninja/raspberry-pi-logo.png",
		iconX2Url: "http://storage.googleapis.com/storage.cl0ud.ninja/raspberry-pi-logo.png",
		iconX3Url: "http://storage.googleapis.com/storage.cl0ud.ninja/raspberry-pi-logo.png")

preferences {
	page(name: "deviceDiscovery", title: "Raspberry Pi UPnP Device Setup", content: "deviceDiscovery")
}

def getSearchTarget() {
	return "urn:schemas-upnp-org:device:RaspberryPi:1"
}

def deviceDiscovery() {
	log.debug "deviceDiscovery"
	def options = [:]
	def devices = getVerifiedDevices()
    log.debug "getVerifiedDevices ${devices}"
	devices.each {
    	log.debug "deviceDiscovery it.value ${it.value}"
		def value = it.value.mac ?: "UPnP Device ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = "${it.value.networkAddress}:${it.value.deviceAddress}"
		options[key] = value
	}

	ssdpSubscribe()

	ssdpDiscover()
	verifyDevices()

	return dynamicPage(name: "deviceDiscovery", title: "Discovery Started!", nextPage: "", refreshInterval: 5, install: true, uninstall: true) {
		section("Please wait while we discover your UPnP Device. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedDevices", "enum", required: false, title: "Select Devices (${options.size() ?: 0} found)", multiple: true, options: options
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	unsubscribe()
	unschedule()

	ssdpSubscribe()

	if (selectedDevices) {
		addDevices()
	}

	runEvery5Minutes("ssdpDiscover")
}

void ssdpDiscover() {
	log.debug("ssdpDiscover")
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${searchTarget}", physicalgraph.device.Protocol.LAN))
}

void ssdpSubscribe() {
	log.debug("ssdpSubscribe")
	subscribe(location, "ssdpTerm.${searchTarget}", ssdpHandler)
}

Map verifiedDevices() {
	log.debug "verified devices"
	def devices = getVerifiedDevices()
	def map = [:]
	log.debug "verified devices ${devices}"
	devices.each {
    	log.debug "verifiedDevices it.value ${it.value}"
		def value = it.value.mac ?: "UPnP Device ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
		def key = "${it.value.networkAddress}:${it.value.deviceAddress}"
		map[key] = value
	}
	map
}

void verifyDevices() {
	log.debug "verifyDevices"
	def devices = getDevices().findAll { it?.value?.verified != true }
    log.debug "verifyDevices ${devices}"
	devices.each {
		int port = convertHexToInt(it.value.deviceAddress)
		String ip = convertHexToIP(it.value.networkAddress)
		String host = "${ip}:${port}"
        log.debug "verifyDevices ${host} ${it.value.ssdpPath}"
		sendHubCommand(new physicalgraph.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
	}
}

def getVerifiedDevices() {
	getDevices().findAll{ it.value.verified == true }
}

def getDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
    log.debug "getDevices ${state.devices}"
	state.devices
}

def addDevices() {
	log.debug "Adding device now!"
	def devices = getDevices()

	selectedDevices.each { dni ->
		def selectedDevice = devices.find { "${it.value.networkAddress}:${it.value.deviceAddress}" == dni }
		def d
		if (selectedDevice) {
			d = getChildDevices()?.find {
				it.deviceNetworkId == "${selectedDevice.value.networkAddress}:${selectedDevice.value.deviceAddress}"
			}
		}

		if (!d) {
			log.debug "Creating Raspberry Pi Monitor with dni: ${selectedDevice.value.networkAddress}:${selectedDevice.value.deviceAddress}"
			addChildDevice("cl0udninja", "Raspberry Pi Monitor", "${selectedDevice.value.networkAddress}:${selectedDevice.value.deviceAddress}", selectedDevice?.value.hub, [
				"label": selectedDevice?.value?.name ?: "Raspberry Pi Monitor",
				"data": [
					"mac": selectedDevice.value.mac,
					"ip": selectedDevice.value.networkAddress,
					"port": selectedDevice.value.deviceAddress
				]
			])
		}
	}
}

def ssdpHandler(evt) {
	log.debug "ssdpHandler ${evt}"
	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseLanMessage(description)
    log.debug "parsedEvent ${parsedEvent}"
	parsedEvent << ["hub":hub]

	def devices = getDevices()
	String deviceId = "${parsedEvent.networkAddress}:${parsedEvent.deviceAddress}"
    log.debug "deviceId ${deviceId}"
	if (devices.get("${deviceId}")) {
		def d = devices.deviceId
		if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
			d.networkAddress = parsedEvent.networkAddress
			d.deviceAddress = parsedEvent.deviceAddress
			def child = getChildDevice(parsedEvent.mac)
			if (child) {
				child.sync(parsedEvent.networkAddress, parsedEvent.deviceAddress)
                //child.refresh()
			}
		}
	} else {
		devices << ["${deviceId}": parsedEvent]
	}
}

void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
	log.debug "deviceDescriptionHandler"
	def body = hubResponse.xml
	log.debug "body ${body.URLBase}"    
	def devices = getDevices()
    log.debug "devices ${devices}"
	def device = devices.find { 
    	def ip = convertHexToIP(it?.key.split(":")[0])
        def port = convertHexToInt(it?.key.split(":")[1])
    	log.debug "key=${ip}:${port}"
    	log.debug "in ${body?.URLBase?.text()}"
        body?.URLBase?.text().contains("${ip}:${port}")
    }
    log.debug "device ${device}"
	if (device) {
		device.value << [name: body?.device?.roomName?.text(), model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNum?.text(), verified: true]
	}
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}