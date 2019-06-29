//DEPRECATED. INTEGRATION MOVED TO SUPER LAN CONNECT

/**
 *  Volumio Device Handler
 */

preferences {		
	input("ip", "string", title:"IP Address", description: "192.168.1.215", defaultValue: "192.168.1.215" ,required: true, displayDuringSetup: true)
}
// for the UI
metadata {
    definition (
    	name: "Volumio",
        namespace: "cl0udninja",
        author: "cl0udninja") {
	
        capability "Music Player"
        capability "Refresh"
        capability "Polling"
        
        attribute "trackDescription", "string"
        
    }
    
    tiles(scale: 2) {
        multiAttributeTile(name:"mediaplayer", type:"mediaPlayer", width:6, height:4) {
			tileAttribute("device.status", key:"PRIMARY_CONTROL") {
				attributeState("stopped", label:"Stopped", defaultState:true)
				attributeState("playing", label:"Playing")
				attributeState("paused", label:"Paused",)
			}
			tileAttribute("device.status", key:"MEDIA_STATUS") {
				attributeState("stopped", label:"Stopped", action:"music Player.play", nextState:"playing")
				attributeState("playing", label:"Playing", action:"music Player.pause", nextState:"paused")
				attributeState("paused", label:"Paused", action:"music Player.play", nextState:"playing")
			}
			tileAttribute("device.status", key:"PREVIOUS_TRACK") {
				attributeState("status", action:"music Player.previousTrack", defaultState:true)
			}
			tileAttribute("device.status", key:"NEXT_TRACK") {
				attributeState("status", action:"music Player.nextTrack", defaultState:true)
			}
			tileAttribute("device.trackDescription", key: "MARQUEE") {
				attributeState("trackDescription", label:"${currentValue}", defaultState:true)
			}
		}
        standardTile("playpause", "device.status", decoration: "flat", canChangeIcon: true) {
        	icon:'https://storage.googleapis.com/storage.cl0ud.ninja/logoFB.png'
            state "paused", label:'Paused', icon:'st.sonos.play-btn', action:'music Player.play'
            state "playing", label:'Playing', icon:'st.sonos.pause-btn', action:'music Player.pause', backgroundColor:"#00a0dc"
            state "stopped", label:'Stopped', icon:'st.sonos.stop-btn', action:'music Player.play'
        }
        standardTile("repeat", "device.repeat", width: 2, height: 1, decoration: "flat") {
            state "no", label: "Repeat", icon:'https://storage.googleapis.com/storage.cl0ud.ninja/long-arrow-alt-right-solid.png', action:"repeat", nextState: "repeat"
            state "repeat", label: "Repeat", icon:'https://storage.googleapis.com/storage.cl0ud.ninja/redo-alt-solid.png', action:"repeat", nextState: "no"
        }
        standardTile("shuffle", "device.shuffle", width: 2, height: 1, decoration: "flat") {
            state "no", label: "Shuffle", icon:'https://storage.googleapis.com/storage.cl0ud.ninja/long-arrow-alt-right-solid.png', action:"shuffle", nextState: "shuffle"
            state "shuffle", label: "Shuffle", icon:'https://storage.googleapis.com/storage.cl0ud.ninja/random-solid.png', action:"repeat", nextState: "no"
        }
        standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 1, decoration: "flat") {
            state "default", action:"refresh", icon: "st.secondary.refresh"
        }
        
        main "playpause"
        details(["mediaplayer", "repeat", "shuffle", "refresh"])
    }
}

def installed() {
	initialize();
}

def updated() {
	initialize();
}

def initialize() {
	sendEvent(name: "checkInterval", value: 30, data: [protocol: "cloud"], displayed: false)
    refresh()
}

def poll() {
	refresh()
}

def refresh() {
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/v1/getState",
		headers: [
        	"HOST": "${ip}:80",
            "Accept": "application/json"
        ],
        null,
        [callback: parse]
    )
    log.debug hubAction.toString()
    hubAction
}

def mute() {
	pause()
    refresh()
}
def nextTrack() {
	sendCommand("next")
    refresh()
}
def pause() {
	sendCommand("pause")
    refresh()
}
def play() {
	sendCommand("play")
    refresh()
}
def previousTrack() {
	sendCommand("prev")
    refresh()
}
def stop() {
	sendCommand("stop")
    refresh()
}
def repeat() {
	sendCommand("repeat")
}
def shuffle() {
	sendCommand("random");
}

private sendCommand(cmd) {
	def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/api/v1/commands/",
        headers: [
            "HOST" : "${ip}:80",
            "Accept": "application/json"
        ],
        query: [cmd: cmd],
        null,
        [callback: parse]
    )
    log.debug result.toString()
    sendHubCommand(result)
    result
}

def parse(description) {
    log.debug "in parse: $description"
    if (!description.hasProperty("body")) {
    	log.debug "Skipping parse"
        return
    }
	def json = parseJson(description?.body)
    log.debug "JSON '${json}'"
    
    if (json.containsKey("status")) {
    	switch(json.status) {
        	case("play"):
	        	sendEvent(name: "status", value: "playing");
        		break;
        	case("stop"):
	        	sendEvent(name: "status", value: "stopped");
        		break;
        	case("pause"):
	        	sendEvent(name: "status", value: "paused");
        		break;
        	default:
	        	sendEvent(name: "status", value: "unkown");
        		break;
        }
    }
    if (json.containsKey("title") && json.containsKey("artist")) {
    	sendEvent(name: "trackDescription", value: "${json.artist} - ${json.title}")
    }
    def repeat = json.repeat ? "repeat" : "no"
    sendEvent(name: "repeat", value: repeat);
    sendEvent(name: "albumart", value: json.albumart);
    take()
}
