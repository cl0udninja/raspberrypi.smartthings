/**
 *  Raspberry Pi Temperature Sensor
 *
 *  Licensed under the GNU v3 (https://www.gnu.org/licenses/gpl-3.0.en.html)
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
preferences {		
	input("ip", "string", title:"IP Address", description: "192.168.1.150", defaultValue: "192.168.1.150" ,required: true, displayDuringSetup: true)		
	input("port", "string", title:"Port", description: "80", defaultValue: "80" , required: true, displayDuringSetup: true)		
}
metadata {
	definition (name: "Raspberry Pi Temperature Sensor (Manual IP)", namespace: "cl0udninja", author: "Janos Elohazi") {
		capability "Temperature Measurement"
        capability "Polling"
		capability "Refresh"
		capability "Sensor"
        capability "Health Check"
        
        command "poll"

	}

	simulator {
		// TODO: define status and reply messages here
	}

	// UI tile definitions
	tiles(scale: 2) {
    	valueTile("temperature", "device.temperature", width: 6, height: 4, canChangeIcon: true) {
            state "temperature", label:'${currentValue}°',
            backgroundColors:[
				[value: 0, color: "#153591"],
				[value: 5, color: "#1e9cbb"],
				[value: 10, color: "#90d2a7"],
				[value: 15, color: "#44b621"],
				[value: 20, color: "#f1d801"],
				[value: 25, color: "#d04e00"],
				[value: 30, color: "#bc2323"],
				[value: 44, color: "#1e9cbb"],
				[value: 59, color: "#90d2a7"],
				[value: 74, color: "#44b621"],
				[value: 84, color: "#f1d801"],
				[value: 95, color: "#d04e00"],
				[value: 96, color: "#bc2323"]                                      
            ]
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }            
		main(["temperature"])
		details(["temperature", "refresh"])
    }
}

def installed() {
	log.debug "installed"
	initialize();
}

def updated() {
	log.debug "updated"
	initialize();
}

def ping() {
	log.debug "ping"
	poll()
}

def initialize() {
	log.debug "initialize"
	sendEvent(name: "checkInterval", value: 60 * 10, data: [protocol: "cloud"], displayed: false)
    refresh()
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
    
    if (getTemperatureScale() == "C") {
    	sendEvent(name: "temperature", value: json.celsius)
    } else {
    	sendEvent(name: "temperature", value: json.fahrenheit)
    }
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    getTemp()
}

def refresh() {
	log.debug "Executing 'refresh'"
    getTemp()
}

private getTemp() {
	def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(port)

    def uri = "/api/temperature"
    def headers=[:]
    headers.put("HOST", "${ip}:${port}")
    headers.put("Accept", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
		headers: headers,
        "${ipHex}:${portHex}",
        [callback: parse]
    )
}

private String convertIPtoHex(ipAddress) {
	log.debug "convertIPtoHex ${ipAddress} to hex"
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	log.debug "convertPortToHex ${port} to hex"
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}
