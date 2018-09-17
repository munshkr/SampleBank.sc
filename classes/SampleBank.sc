SampleBank {
	classvar <dir, <buffers;

	*initClass {
		buffers = Dictionary.new;
		this.addEventType
	}

	*init { |argDir, server|
		dir = argDir;
		if (dir.isNil) { Error("samples path is missing").throw };
		server = server ? Server.default;

		// If server is the default and is not currently running,
		// make sure that buffers are created on boot.
		if (server == Server.default) {
			ServerBoot.add({ this.makeBuffers(server) })
		};

		// Otherwise, do it now...
		if (server.serverRunning) {
			this.makeBuffers(server)
		};
	}

	*makeBuffers { |server|
		server = server ? Server.default;
		buffers.clear;

		PathName(dir).entries.do { |subfolder|
			var entries = subfolder.entries.select { |entry| entry.extension == "wav" }.collect { |entry|
				Buffer.readChannel(server, entry.fullPath, channels: [0])
			};
			if (entries.isEmpty.not) {
				buffers.add(subfolder.folderName.asSymbol -> entries)
			}
		}
	}

	*list {
		^this.buffers.keys
	}

	*displayList {
		^this.buffers.keysValuesDo { |bankName, buffers|
			"% [%]".format(bankName, buffers.size).postln
		}
	}

	*get { |bank, index|
		if (this.buffers.isNil.not) {
			var bufList = this.buffers[bank.asSymbol];
			if (bufList.isNil.not) {
				index = index % bufList.size;
				"bufList[%] = %".format(index, bufList[index]).postln;
				^bufList[index]
			}
		};
		^nil
	}

	*addEventType {
		Event.addEventType(\sample, {
			if (~buf.isNil) {
				if (~bank.isNil.not && ~index.isNil.not) {
					~buf = SampleBank.get(~bank, ~index)
				} {
					if (~sample.isNil.not) {
						var pair, bank, index;
						pair = ~sample.split($:);
						bank = pair[0].asSymbol;
						index = if (pair.size == 2) { pair[1].asInt } { 0 };
						~buf = SampleBank.get(bank, index)
					}
				}
			};
			~type = \note;
			currentEnvironment.postln.play
		})
	}
}