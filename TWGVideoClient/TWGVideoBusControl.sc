TWGVideoBusControl {
	var <parent, <index, busSym;
	var <media, <position, <speed, <db, <transport, routing;

	*new {|parent, index|
		^super.newCopyArgs(parent, index).init;
	}

	init {
		transport = \paused;
		busSym = switch (index,
			0, {\a},
			1, {\b},
			2, {\c},
			3, {\d},
			4, {\e}
		)
	}

	set {|...pairs|
		parent.set(busSym, pairs)
	}

	play {|...pairs|
		parent.set(busSym, [\transport, \playing] ++ pairs)
	}

	pause {|...pairs|
		parent.set(busSym, [\transport, \paused] ++ pairs)
	}

	ff { |setFFspeed ...pairs|
		parent.set(\ffspeed, setFFspeed, busSym, [\transport, \ff] ++ pairs)
	}

	rw { |setFFspeed ...pairs|
		parent.set(\ffspeed, setFFspeed, busSym, [\transport, \rw] ++ pairs)
	}

	cue { |media, position, speed, db|
		this.play(
			\media, media ?? 1,
			\position, position ?? 0,
			\speed, speed ?? 1,
			\db, db ?? 0
		)
	}

	reset { |...pairs|
		this.pause(\media, 0, \position, 0, \speed, 1, \db, 0, *pairs);
	}

	media_ { |val, hard = true|
		if (media != val) {
			if (hard) {this.set(\media, val)};
			media = val;
			{parent.client.gui.bMediaMenu[index].value_(val)}.defer;
		}
	}

	position_ { |val, hard = true|
		if (position != val) {
			if (hard) {this.set(\position, val)};
			position = val;
			{parent.client.gui.setPos(index, val)}.defer;
		}
	}

	speed_ { |setSpeed, ramp, hard = true|
		var curve = 1;
		if (setSpeed.isArray && ramp.isNil) {
			curve = setSpeed[2] ?? 1;
			ramp = setSpeed[1] ?? 0;
			setSpeed = setSpeed[0];
		};
		if (speed != setSpeed) {
      if (hard) {this.set(\speed, [setSpeed, ramp, curve])};
			speed = setSpeed;
			{parent.client.gui.bSpeedNum[index].value_(speed)}.defer;
		}
	}

	db_ { |val, hard = true|
		if (db != val) {
			if (hard) {this.set(\db, val)};
			db = val;
			{parent.client.gui.setGain(index, db)}.defer;
		}
	}

	transport_ { |val, hard = true|
		if (transport != val) {
			if (hard) {this.set(\transport, val)};
			transport = val;
			{parent.client.gui.setTransport(index, transport)}.defer;
		}
	}

	routing {
		^parent.routing[busSym];
	}

	routing_ { |...vals|
		this.parent.set(busSym, [\routing, vals]);
	}

	route { |...vals|
		this.parent.route(busSym, vals)
	}

	unroute { |...vals|
		this.parent.unroute(busSym, vals)
	}
}