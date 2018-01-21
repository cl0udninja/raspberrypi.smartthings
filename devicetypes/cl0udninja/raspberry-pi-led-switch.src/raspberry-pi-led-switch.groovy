/**
 *  Raspberry Pi Monitor
 *
 *  Monitor your Raspberry Pi using SmartThings and Raspberry Pi Monitor <https://github.com/cl0udninja/raspberrypi.monitor>
 *
 *  Licensed under the GNU v3 (https://www.gnu.org/licenses/gpl-3.0.en.html)
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Raspberry Pi LED Switch", namespace: "cl0udninja", author: "Janos Elohazi") {
		capability "Polling"
		capability "Refresh"
		capability "Actuator"
		capability "Switch"

		command "turnOffLed"
        command "turnOnLed"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles() {
		standardTile("toggleLed", "device.switch", width: 3, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', icon: "st.Electronics.electronics18", backgroundColor: "#ffffff", nextState: "on", action: "turnOnLed"
			state "on", label: 'On', icon: "st.Electronics.electronics18", backgroundColor: "#79b821", nextState: "off", action: "turnOffLed"
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 1, height: 1, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main "toggleLed"
        
        details(["toggleLed", "refresh"])
    }
}

def turnOnLed() {
	log.debug "Device network id: ${device.deviceNetworkId}"
    def parts = device.deviceNetworkId.split(":")
    if (parts.length !=2) {
    	log.warn "Can't figure out ip and port for device: ${device.id}"
    }
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    
    def uri = "/api/led"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    headers.put("Content-type", "application/json")
    def body = "{\"pinState\":\"HIGH\"}"
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: uri,
		headers: headers,
        body: body,
        device.deviceNetworkId,
        [callback: getLedState]
    )
    log.debug "Turning on led ${hubAction}"
    hubAction
}

def turnOffLed() {
	log.debug "Device network id: ${device.deviceNetworkId}"
    def parts = device.deviceNetworkId.split(":")
    if (parts.length !=2) {
    	log.warn "Can't figure out ip and port for device: ${device.id}"
    }
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    
    def uri = "/api/led"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    headers.put("Content-type", "application/json")
    def body = "{\"pinState\":\"LOW\"}"
    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: uri,
		headers: headers,
        body: body,
        device.deviceNetworkId,
        [callback: getLedState]
    )
    log.debug "Turning off led ${hubAction}"
    hubAction
}

// parse events into attributes
def parse(description) {
	log.debug "Parse ${description}"
    if (!description.hasProperty("body")) {
    	log.debug "Skipping parse"
        return
    }
    log.debug "Parsing '${description?.body}'"
	def msg = parseLanMessage(description?.body)
    log.debug "Msg ${msg}"
	def json = parseJson(description?.body)
    log.debug "JSON '${json}'"
    
    if (json.containsKey("pinState")) {
    	sendEvent(name: "switch", value: "HIGH".equals(json.pinState) ? "on" : "off")
    }
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getLedState()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getLedState()
}

private getLedState() {
	log.debug "Device network id: ${device.deviceNetworkId}"
    def parts = device.deviceNetworkId.split(":")
    if (parts.length !=2) {
    	log.warn "Can't figure out ip and port for device: ${device.id}"
    }
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    
    def uri = "/api/led"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        device.deviceNetworkId,
        [callback: parse]
    )
    log.debug "Getting Pi data ${hubAction}"
    hubAction 
}

private String convertIPtoHex(ip) {
	log.debug "convertIPtoHex ${ip} to hex"
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	log.debug "convertPortToHex ${port} to hex"
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
def sync(ip, port) {
	log.debug "sync ${ip} ${port}"
	def existingIp = getDataValue("ip")
	def existingPort = getDataValue("port")
	if (ip && ip != existingIp) {
		updateDataValue("ip", ip)
	}
	if (port && port != existingPort) {
		updateDataValue("port", port)
	}
    def ipHex = convertIPToHex(ip)
    def portHex = convertPortToHex(port)
    device.deviceNetworkId = "${ipHex}:${portHex}"
}