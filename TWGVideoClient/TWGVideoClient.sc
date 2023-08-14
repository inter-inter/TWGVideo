TWGVideoClient {
	var <name, <>serverAddress, <mode;
	var <gui, <ping, <connected, <>control;

	*new {|name, serverAddress, mode = \control, connectMIDI = false|
    ^super.newCopyArgs(name, serverAddress, mode).init(connectMIDI);
	}

	init { |connectMIDI|
		gui = TWGVideoClientGUI(this);
		this.mode_(mode);
		connected = false;
		this.connect;

		OSCdef(\fromvideo, { |msg|
			var key = msg[1], bus, val;
			var busKeys = [\a, \b, \c, \d, \e];
			var chanKeys = [\ear1, \ear2, \ear3, \room, \xtra, \phones];
			connected = true;
			{gui.connectedText.string = "Connected to" + serverAddress.hostname.asString}.defer;
			//msg.postln;
			switch (key,
				\showinfo, {{
					gui.showText.string = "Show: " + (msg[2] ?? "");
					gui.bMediaMenu.do({ |menu, index|
						menu.items_([""]++msg[3..].collect({|item, i| (i+1).asString ++ "  " ++ item})).value_(control.buses[index].media ?? 0);
						}
					);
				}.defer},
				\transport, {
					bus = msg[2];
					val = (0: \paused, 1: \playing, -1: \rw, 2: \ff)[msg[3]];
					control.buses[bus].transport_(val, hard: false);
				},
				\preset, {
					control.preset_(msg[2], hard: false)
				},
				\media, {
					# bus, val = msg[2..3];
					control.buses[bus].media_(val, hard: false);
				},
				\pos, {
					# bus, val = msg[2..3];
					control.buses[bus].position_(val, hard: false);
				},
				\speed, {
					# bus, val = msg[2..3];
					control.buses[bus].speed_(val, hard: false);
				},
				\db, {
					# bus, val = msg[2..3];
					control.buses[bus].db_(val, hard: false);
				},
				\level, {
					var side, peakLevel, rmsLevel;
					# bus, side, peakLevel, rmsLevel = msg[2..5];
					{
						gui.bLevel[bus][side].peakLevel = peakLevel.ampdb.linlin(-80, 0, 0, 1, \min);
						gui.bLevel[bus][side].value = rmsLevel.ampdb.linlin(-80, 0, 0, 1)
					}.defer;
				},
				\routing, {
					var routingPairs = [];
					msg[2..].clump(3).do({ |cell|
						var bus = busKeys[cell[0]];
						var chan = chanKeys [cell[1]];
						var val = cell[2];
						if (val == 1) {routingPairs = routingPairs.addAll([bus, chan])};
					});
					control.routing_(*routingPairs ++ false);
				},
				\route, {
					var bus = busKeys[msg[2]];
					var chan = chanKeys[msg[3]];
					var val = msg[4];
					case
					{val == 1} {control.route(bus, chan, false)}
					{val == 0} {control.unroute(bus, chan, false)}
				},
				\ffspeed, {
					control.ffspeed_(msg[2], hard: false)
				}
			);
		}, '/fromvideo');


		// midi from AKAI
    if (connectMIDI) {
      MIDIClient.init;
      MIDIIn.connectAll;
      MIDIdef.noteOn(\akai, {|val, num, chan, src|
        num.postln;
        defer {
          case
          { num == 48 } { gui.gRWTog.valueAction_(1) }
          { num == 49 } { gui.gFFTog.valueAction_(1) }
          { num == 50 } { gui.gPPTog.valueAction_(1) }
          { num == 46 } { gui.gPPTog.valueAction_(0) }
          { num == 40 } { gui.bRWTog[0].valueAction_(1) }
          { num == 41 } { gui.bFFTog[0].valueAction_(1) }
          { num == 42 } { gui.bRWTog[1].valueAction_(1) }
          { num == 43 } { gui.bFFTog[1].valueAction_(1) }
          { num == 36 } { gui.bPPTog[0].valueAction_(1) }
          { num == 37 } { gui.bPPTog[0].valueAction_(0) }
          { num == 38 } { gui.bPPTog[1].valueAction_(1) }
          { num == 39 } { gui.bPPTog[1].valueAction_(0) }
        }
      }, chan: 9);
    };


		^control = TWGVideoControl(this);
	}

	connect {
		serverAddress.sendMsg('/ping', name);
	}

	mode_ {|setmode|
		mode = setmode;
		{gui.modeTog.valueAction_(1-(mode==\control).asInteger)}.defer;
	}

	quit {
		connected = false;
		gui.win.close;
		serverAddress.sendMsg('/quit', name);
	}

}