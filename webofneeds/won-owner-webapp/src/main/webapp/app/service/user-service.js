needServiceModule = angular.module('owner.service.user', []);

needServiceModule.factory('userService', function ($window, $http) {

	var user = {};
	user = $window.user;

	return {
		isAuth:function () {
			return (user.isAuth == true);
		},
		setAuth:function(username) {
			user.isAuth = true;
			user.username = username;
		},
		getUserName:function () {
			return user.username;
		},
		resetAuth:function () {
			user = {
				isAuth : false
			}
		},
		registerUser : function(user) {
			return $http.post(
					'/owner/rest/user/',
					user
			).then(
				function() {
					// success
					return {status : "OK"};
				},
				function(response) {
					switch (response.status) {
					case 409:
						// normal error
						return {status:"ERROR", message: "Username is already used"};
					break;
					default:
						// system error
						console.log("FATAL ERROR");
					break;
					}
				}
			);
		},
		logIn : function(user) {
			return $http.post(
					'/owner/rest/user/login',
					user
			).then(
				function () {
					return {status:"OK"};
				},
				function (response) {
					switch (response.status) {
						case 403:
							// normal error
							return {status:"ERROR", message:"Bad username or password"};
						break;
						default:
							// system error
							console.log("FATAL ERROR");
						break;
					}
				}
			);
		},
		logOut : function() {
			return $http.post(
					'/owner/rest/user/logout'
			).then(
				function (data, status) {
					return {status:"OK"};
				},
				function (data, status) {
					console.log("FATAL ERROR");
				}
			);
		}
	}
});