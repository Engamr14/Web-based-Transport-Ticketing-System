# land-login
LoginService microservice

# possible schema

-POST auth/user/verify (Removes Activation and updates User.is_active to true)
-POST auth/user/register (Inserts Activation and User)

-POST auth/login (Return a jwt with the role of the User and a lifetime of the token if hash matches the password)

-POST auth/admin/enroll (Inserts a new user with role admin)
-POST auth/system/enroll (Inserts a new user with role system that corresponds to a qrreader?)