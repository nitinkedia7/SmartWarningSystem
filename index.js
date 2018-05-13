// <header>
// Module:         sendAlert
// Date of creation: 14-04-18
// Author:			  Nitin Kedia
// Modification history: 
// 	14-04-18: Created module with initialization functions
//  15-04-18: Implemented different messages based on state and token retrieval.
// 	16-04-18: Documented code.
// Synopsis:
// This is a helper of NotificationActivity. The sender function triggers when it detects a state change in a database,
// then generates, sends and saves a notification if new state is less than 8.
// Global variables: None
// Functions:
//    sendNotification()
//    generateAlert()
// </header>

'use strict';
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
/** Triggers when the state of a joined student changes and sends a notification according to new state. **/
exports.sendNotification = functions.database.ref('/Sessions/{sessionName}/joinedUsers/{Uid}/state') // Path where current student states are saved.
    .onWrite((change, context) => {
      const Uid = context.params.Uid;
      const sessionName = context.params.sessionName;
      const state = change.after.val();
      // If database update was not done properly we exit the function.
      if (!state) {
        console.log('Could not save new state of user with Uid ', Uid)
        return 0
      }
      else {
        console.log('New state of user with UID : ', Uid, 'is',  change.after.val());
        if (Number(state) > 7) {
          console.log('No need to send notification')
          return 0
        }
      }
      // Build the notification for student
      const alert = generateAlert(Number(state))
      const alertPromise = admin.database().ref(`/Sessions/${sessionName}/alerts/${Uid}/sentAlerts`).push().set(alert);

      // Get the device notification token of student.
      const getDeviceTokensPromise = admin.database().ref(`Students/${Uid}/token`).once('value');

      return Promise.all([getDeviceTokensPromise, alertPromise]).then(results => {
        let token = results[0].val();
        if (token === "") return console.log("Invalid FCM token")
        else console.log('FCM token is ', token);                
        // Notification details.
        const payload = {
          notification: {
            title: 'Attentiveness Alert',
            body: alert['comment'],
          },
          token: token
        };
        // Send notifications to fetched token.
        return admin.messaging().send(payload);
      }).then((response) => {
        // Response is a message ID string.
        console.log('Successfully sent message:', response);
        return Promise.all([]);
      })
      .catch((error) => {
        console.log('Error sending message:', error);
      });
    });
    // Based on current state generate an appropriate alert
    function generateAlert(state) {
      var message = ""
      if (state <= 4) {
        message = "Please pay attention in class."
      }
      else if (state <= 7) {
        message = "Try to be a bit more attentive."
      }
      var alert = {
        time: '10',
        status: 'Enabled',
        comment: message,
        state: `${state}`,
      };
      return alert;
    }