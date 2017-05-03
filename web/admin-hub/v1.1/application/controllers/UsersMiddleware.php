<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class UsersMiddleware extends REST_Controller{

    public function __construct(){
        parent::__construct();
        $this->load->model('UsersModel');
    }
    
    public function index_get(){
        
        $this->load->view('user');
        
    }

    public function editindex_get(){

        $this->load->view('user_edit');
        
    }
    
    public function orderfind_get($orderId){
        
        $results = $this->UsersModel->getSellerByOrder($orderId);            
        echo json_encode($results, JSON_UNESCAPED_UNICODE);
        
    } 

    public function find_get($id){

        $user = $this->UsersModel->getUserById($id);
        echo json_encode($user, JSON_UNESCAPED_UNICODE);

    }

    public function all_get(){

        $users = $this->UsersModel->getUsers();
        echo json_encode($users, JSON_UNESCAPED_UNICODE);

    }

    public function update_post(){

        $user = json_decode($this->post('user'));
        if ($this->UsersModel->updateUser($user)) {
            print 'Actualizado';
        } else {
            print 'Error';
        }

    }

    public function updatepass_post(){

        $password = $this->post('password');
        $id = $this->post('id');
        $response = $this->UsersModel->updatePassword($password, $id);

        if ($response) {
            print json_encode(array(
                'mensaje'=>'1'
            ));
        } else {
            print json_encode(array(
                'mensaje'=>'2'
            ));
        }
    }

}