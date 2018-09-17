SampleBank {
	classvar <dir, <buffers;

	const <supportedExtensions = #[\wav, \aiff];

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
			var entries;
			entries = subfolder.entries.select { |entry| supportedExtensions.includes(entry.extension.asSymbol) };
			entries = entries.collect { |entry|
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
				^bufList[index]
			}
		};
		^nil
	}

	*addEventType {
		Event.addEventType(\sample, {
			if (~buf.isNil && ~bank.isNil.not) {
				var index = ~index ? 0;
				~buf = SampleBank.get(~bank, index)
			};
			~type = \note;
			currentEnvironment.play
		})
	}
}