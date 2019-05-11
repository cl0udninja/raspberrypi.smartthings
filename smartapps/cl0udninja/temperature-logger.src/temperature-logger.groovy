/**
 *  Temperature Logger
 *
 *  Copyright 2019 Janos Elohazi
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
    name: "Temperature Logger",
    namespace: "cl0udninja",
    author: "Janos Elohazi",
    description: "Logs temperatures over RestAPI calls",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png")


preferences {
	section("Select Temperature Sensors") {
        input "tempSensors", "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true,   required: true
    }
    section("Enter endpoint URI (ie.: http:mydomain.com:8080/api/temprecord") {
        input "ip", "text", title:"Endpoint IP", required: true, multiple: false
        input "port", "number", title:"Endpoint port", required: true, multiple: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	runEvery30Minutes(reportTemp)
    reportTemp()
}

def reportTemp() {
	for (tempSensor in tempSensors) {
    	log.trace("${tempSensor.getDisplayName()} - ${tempSensor.currentValue("temperature")}")
        def result = new physicalgraph.device.HubAction(
        	method: "POST",
            path: "/api/temprecord",
            body: "{\"name\":\"${tempSensor.getDisplayName()}\", \"temperature\":${tempSensor.currentValue("temperature")}}",
            headers: [
                "HOST" : "${ip}:${port}",
                "Content-Type": "application/json"
            ],
            null,
            [callback: parse]
	)
    log.debug result.toString()
    sendHubCommand(result);
    }  
}

def parse(physicalgraph.device.HubResponse hubResponse) {
    log.debug "in parse: $hubResponse"
    log.debug "hubResponse json: ${hubResponse.json}"
}