# karaoke-ivs-app-android

## How to test the real-time SDK sample app

- Create an AWS account (talk to Cliff at Synervoz)
- Create a Stage in IVS Console
- Add two participants
- Copy token of first participant's token into `token` when instantiating `Stage` in `createStage`
- Start streaming
- Copy token of the second participant's token into Participant field in CodePen (https://codepen.io/Tayyab-Javed-the-scripter/project/details/XaYwVY)
