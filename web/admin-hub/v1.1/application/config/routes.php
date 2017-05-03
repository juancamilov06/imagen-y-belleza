<?php
defined('BASEPATH') OR exit('No direct script access allowed');

/*
| -------------------------------------------------------------------------
| URI ROUTING
| -------------------------------------------------------------------------
| This file lets you re-map URI requests to specific controller functions.
|
| Typically there is a one-to-one relationship between a URL string
| and its corresponding controller class/method. The segments in a
| URL normally follow this pattern:
|
|	example.com/class/method/id/
|
| In some instances, however, you may want to remap this relationship
| so that a different class/function is called than the one
| corresponding to the URL.
|
| Please see the user guide for complete details:
|
|	https://codeigniter.com/user_guide/general/routing.html
|
| -------------------------------------------------------------------------
| RESERVED ROUTES
| -------------------------------------------------------------------------
|
| There are three reserved routes:
|
|	$route['default_controller'] = 'welcome';
|
| This route indicates which controller class should be loaded if the
| URI contains no data. In the above example, the "welcome" class
| would be loaded.
|
|	$route['404_override'] = 'errors/page_missing';
|
| This route will tell the Router which controller/method to use if those
| provided in the URL cannot be matched to a valid route.
|
|	$route['translate_uri_dashes'] = FALSE;
|
| This is not exactly a route, but allows you to automatically route
| controller and method names that contain dashes. '-' isn't a valid
| class or method name character, so it requires translation.
| When you set this option to TRUE, it will replace ALL dashes in the
| controller and method URI segments.
|
| Examples:	my-controller/index	-> my_controller/index
|		my-controller/my-method	-> my_controller/my_method
*/
$route['default_controller'] = 'welcome';
$route['404_override'] = '';
$route['translate_uri_dashes'] = FALSE;

/*
| -------------------------------------------------------------------------
| REST API Routes
| -------------------------------------------------------------------------
*/
//Hub Routes
$route['main']['get'] = 'MainMiddleware/index';
$route['inventory']['get'] = 'InventoryMiddleware/index';

//Basic API Routes
$route['basic']['get'] = 'BasicMiddleware/all';
$route['basic/update_check']['post'] = 'BasicMiddleware/updatecheck';
$route['basic/update']['post'] = 'BasicMiddleware/update';
$route['basic/verify']['get'] = 'BasicMiddleware/verify';
$route['basic/messages']['get'] = 'BasicMiddleware/messages';
$route['basic/messages']['post'] = 'BasicMiddleware/messages';
$route['basic/login']['post'] = 'BasicMiddleware/login';

//Items API Routes
$route['items']['get'] = 'ItemsMiddleware/index';
$route['items/create']['get'] = 'ItemsMiddleware/indexcreate';
$route['items/new']['post'] = 'ItemsMiddleware/create';
$route['items/all']['get'] = 'ItemsMiddleware/all';
$route['items/find/(:num)']['get'] = 'ItemsMiddleware/find/$1';
$route['items/update']['post'] = 'ItemsMiddleware/update';


//Bill API Routes
$route['bill/unbilled']['get'] = 'BillMiddleware/unbilled';
$route['bill/update']['post'] = 'BillMiddleware/update';
$route['bill/update_all']['post'] = 'BillMiddleware/updateall';
$route['bill/finish']['post'] = 'BillMiddleware/finish';


//Client API Routes
$route['clients']['get'] = 'ClientsMiddleware/index';
$route['clients/all']['get'] = 'ClientsMiddleware/all';
$route['clients/add']['post'] = 'ClientsMiddleware/create';
$route['clients/find/(:num)']['get'] = 'ClientsMiddleware/find/$1';
$route['clients/update']['post'] = 'ClientsMiddleware/update';
$route['clients/add_all']['post'] = 'ClientsMiddleware/createall';
$route['clients/types/update']['post'] = 'ClientsMiddleware/typesupdate';
$route['clients/types']['get'] = 'ClientsMiddleware/types';

//Count API Routes
$route['count']['get'] = 'CountMiddleware/count';

//Order API Routes
$route['orders/detail']['get'] = 'OrderMiddleware/detailindex';
$route['orders/all']['get'] = 'OrderMiddleware/all';
$route['orders/hub_all']['get'] = 'OrderMiddleware/huball';
$route['orders/create']['post'] = 'OrderMiddleware/create';
$route['orders/storage/all']['get'] = 'OrderMiddleware/storageall';
$route['orders/hub_update']['post'] = 'OrderMiddleware/hubupdate';
$route['orders/(:num)/detail']['get'] = 'OrderMiddleware/detail/$1';

//Order Items API Routes
$route['orders/items/update']['post'] = 'OrderItemMiddleware/create';
$route['orders/(:num)/items']['get'] = 'OrderItemMiddleware/find/$1';

//User API Routes
$route['users']['get'] = 'UsersMiddleware/index';
$route['users/edit']['get'] = 'UsersMiddleware/editindex';
$route['users/all']['get'] = 'UsersMiddleware/all';
$route['users/orders/(:num)']['get'] = 'UsersMiddleware/orderfind/$1';
$route['users/update_pass']['post'] = 'UsersMiddleware/updatepass';
$route['users/update']['post'] = 'UsersMiddleware/update';
$route['users/find/(:num)']['get'] = 'UsersMiddleware/find/$1';

//Location API Routes
$route['location/all']['get'] = 'LocationMiddleware/all';
$route['location/create']['post'] = 'LocationMiddleware/create';

//Brands API Routes
$route['brands']['get'] = 'BrandsMiddleware/index';
$route['brands/all']['get'] = 'BrandsMiddleware/all';
$route['brands/find/(:num)']['get'] = 'BrandsMiddleware/find/$1';
$route['brands/update']['post'] = 'BrandsMiddleware/update';

//Categories API Routes
$route['categories']['get'] = 'CategoriesMiddleware/index';
$route['categories/all']['get'] = 'CategoriesMiddleware/all';
$route['categories/update']['post'] = 'CategoriesMiddleware/update';

//Disposal API Routes
$route['disposal']['get'] = 'DisposalMiddleware/index';
$route['disposal/create']['post'] = 'DisposalMiddleware/create';

//Entering API Routes
$route['entering']['get'] = 'EnteringMiddleware/index';
$route['entering/create']['post'] = 'EnteringMiddleware/create';