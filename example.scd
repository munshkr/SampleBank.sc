// Set up SampleBank by pointing it to your sample bank directory
SampleBank.loadAll(thisProcess.platform.userHomeDir ++ "/Samples/");

// Buffers are created automatically at server boot
s.boot;

// Print on post window the list of sample banks
SampleBank.displayList;
/*
bd [3]
808 [10]
909 [9]
mark [13]
...
*/

// For example, you can use buffers directly like this
SampleBank.buffers[\bd][0].play
// also with the .get method:
SampleBank.get(\bd, 1)

// The index parameter wraps around if trying to play a non-existant buffer in
// bank.  For example, "bd" has 3 samples only, so trying to read sample 4 will
// give you sample 0:
SampleBank.get(\bd, 3) == SampleBank.get(\bd, 0)
SampleBank.get(\bd, 4) == SampleBank.get(\bd, 1)
// ...

// SampleBank adds a new Event type called \sample, which will try to add a \buf
// key automatically based on some other keys.
//
// Suppose you have this Synthdef for playing mono samples
(
SynthDef(\vplaym, { |out=0, buf=0, rate=1, amp=0.5, pan=0, atk=0.01, rel=1, pos=0|
	var sig, env;
	sig = Pan2.ar(PlayBuf.ar(1, buf, BufRateScale.ir(buf) * rate, 1, BufDur.kr(buf) * pos * s.sampleRate, doneAction: 2), pan);
	env = EnvGen.ar(Env.linen(0.0, rel, 0.0, 1));
	sig = sig * env;
	sig = sig * amp;
	Out.ar(out, sig)
}).add;
)

// You can now provide \bank and \index, and the \buf argument will be fetched
// automatically:
(
x = Pbind(\instrument, \vplaym,
	\type, \sample,
	\bank, Pseq([\klang, \Sutra, \mark], inf),
	\index, Pshuf((0..15), inf),
	\dur, 0.125).play;
)
x.stop;

// Also, there's the \sample key, which will be a string containing both bank name and
// index separated by a colon (`:`):
(
x = Pbind(\instrument, \vplaym,
	\type, \sample,
	\sample, Pseq(["klang:3", "Sutra:1", "Sutra"], inf),
	\dur, 0.125).play
)

// Finally, you can free buffers. This will also stop loading buffers on server boot.
SampleBank.free
