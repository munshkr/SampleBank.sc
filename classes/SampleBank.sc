SampleBank {
	classvar <dir, <buffers;
	classvar makeBuffersFn;

	const <supportedExtensions = #[\wav, \aiff];

	*initClass {
		buffers = Dictionary.new;
		makeBuffersFn = #{ |server| SampleBank.prMakeBuffers(server) };
		this.prAddEventType
	}

	*loadAll { |argDir, server|
		dir = argDir;
		if (dir.isNil) { Error("samples dir is missing").throw };
		server = server ? Server.default;

		// Make sure that buffers are created on boot always
		ServerBoot.add(makeBuffersFn, server);

		// Now, if server is running, load them right now
		if (server.serverRunning) {
			this.prMakeBuffers(server)
		};
	}

	*free { |server|
		this.prFreeBuffers;
		server = server ? Server.default;
		ServerBoot.remove(makeBuffersFn, server);
		"Sample banks freed".postln;
	}

	*get { |bank, index|
		if (buffers.isNil.not) {
			var bufList = buffers[bank.asSymbol];
			if (bufList.isNil.not) {
				index = index % bufList.size;
				^bufList[index]
			}
		};
		^nil
	}

	*list {
		^buffers.keys
	}

	*displayList {
		^buffers.keysValuesDo { |bankName, buffers|
			"% [%]".format(bankName, buffers.size).postln
		}
	}

	*prFreeBuffers {
		buffers.do { |banks|
			banks.do { |buf|
				if (buf.isNil.not) {
					buf.free
				}
			}
		};
		buffers.clear;
	}

	*prMakeBuffers { |server|
		this.prFreeBuffers;

		PathName(dir).entries.do { |subfolder|
			var entries;
			entries = subfolder.entries.select { |entry|
				supportedExtensions.includes(entry.extension.asSymbol)
			};
			entries = entries.collect { |entry|
				Buffer.readChannel(server, entry.fullPath, channels: [0])
			};
			if (entries.isEmpty.not) {
				buffers.add(subfolder.folderName.asSymbol -> entries)
			}
		};

		"% sample banks loaded".format(buffers.size).postln;
	}

	*prAddEventType {
		Event.addEventType(\sample, {
			if (~buf.isNil) {
				var bank = ~bank;
				if (bank.isNil.not) {
					var index = ~index ? 0;
					~buf = SampleBank.get(bank, index)
				} {
					var sample = ~sample;
					if (sample.isNil.not) {
						var pair, bank, index;
						pair = sample.split($:);
						bank = pair[0].asSymbol;
						index = if (pair.size == 2) { pair[1].asInt } { 0 };
						~buf = SampleBank.get(bank, index)
					}
				}
			};
			~type = \note;
			currentEnvironment.play
		})
	}
}