'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.database
    .ref('/notifications/{user_id}/{notification_id}')
    .onWrite((change, context) => {
        const user_id = context.params.user_id;
        const notification_id = context.params.notification_id;

        console.log('The user Id is : ', user_id);

        if (!change.after.val()) {
            return console.log('A Notification has been deleted from the database : ', notification_id);
        }

        const fromUser = admin.database()
        .ref(`/notifications/${user_id}/${notification_id}`).once('value');

        return fromUser.then(fromUserResult => {

            const from_user_id = fromUserResult.val().from;

            console.log('You have a new notification from  : ', from_user_id);

            const userQuery = admin.database().ref(`users/${from_user_id}/name`).once('value');
            const deviceToken = admin.database().ref(`users/${user_id}/device_token`).once('value');

            return Promise.all([userQuery, deviceToken]).then(result => {

                const userName = result[0].val();
                const token_id = result[1].val();

                const payload = {
                    notification: {
                        title: "New Friend Request",
                        body: `${userName} has sent you a friend request`,
                        icon: "default",
                        click_action: "open_profile"
                    },
                    data: {
                        from_user_id: from_user_id
                    }
                };

                return admin.messaging().sendToDevice(token_id, payload).then(response => {
                    return console.log('Notification successfully executed');
                });
            });
        });
    });


    exports.sendRide = functions.database
        .ref('/rides/{user_id}/{notification_id}')
        .onWrite((change, context) => {
            const user_id = context.params.user_id;
            const notification_id = context.params.notification_id;

            console.log('The rider Id is : ', user_id);

            if (!change.after.val()) {
                return console.log('A Ride has been deleted from the database : ', notification_id);
            }

            const fromUser = admin.database()
            .ref(`/rides/${user_id}/${notification_id}`).once('value');

            return fromUser.then(fromUserResult => {

                const from_user_id = fromUserResult.val().from;
                const address_user_id = fromUserResult.val().address;

                console.log('You have a new ride from  : ', from_user_id);
                console.log('The address is  : ', address_user_id);

                const userQuery = admin.database().ref(`users/${from_user_id}/name`).once('value');
                const deviceToken = admin.database().ref(`users/${user_id}/device_token`).once('value');

                return Promise.all([userQuery, deviceToken]).then(result => {

                    const userName = result[0].val();
                    const token_id = result[1].val();

                    const payload = {
                        notification: {
                            title: "New Ride Request",
                            body: `${userName} has sent you a ride request`,
                            icon: "default",
                            click_action: "open_map"
                        },
                        data: {
                            from_user_id: from_user_id,
                            address_user_id: address_user_id
                        }
                    };

                    return admin.messaging().sendToDevice(token_id, payload).then(response => {
                        return console.log('Ride successfully executed');
                    });
                });
            });
        });
