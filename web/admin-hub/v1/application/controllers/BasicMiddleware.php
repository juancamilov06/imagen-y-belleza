<?php

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class BasicMiddleware extends REST_Controller
{
    public function __construct(){
        parent::__construct();
        $this->load->model('BasicModel');
    }

    public function login_post(){

        $username = $this->post("username");
        $password = hash('sha256', $_POST["password"]);

        $results = $this->BasicModel->login($username, $password);
        $encodedResult = json_encode($results, JSON_UNESCAPED_UNICODE);
        $response = new stdClass();

        if(!empty(json_decode($encodedResult,1))) {
            $response->success = 'true';
            $response->data = $results;
            print json_encode($response, JSON_UNESCAPED_UNICODE);
        } else {
            $response->success = 'false';
            print json_encode($response, JSON_UNESCAPED_UNICODE);
        }
        
    }

    public function all_get(){

        $data = $this->BasicModel->getAll();
        echo json_encode($data, JSON_UNESCAPED_UNICODE);

    }

    public function messages_post(){
        
        $response = $this->BasicModel->setAllRead();
        if ($response) {
            print json_encode(array(
                'mensaje'=>'Creacion exitosa'
            ));
        } else {
            print json_encode(array(
                'mensaje'=>'Creacion fallida'
            ));
        }

    }

    public function messages_get(){

        $unread = $this->get('unread');

        if ($unread == "true") {
            echo json_encode($this->BasicModel->getUnreadMessages(), JSON_UNESCAPED_UNICODE);
        } else {
            echo json_encode($this->BasicModel->getAllMessages(), JSON_UNESCAPED_UNICODE);
        }

    }

    public function verify_get(){
        echo 'ok';
    }

    public function update_post(){

        $date = $this->post('date');
        $data = $this->BasicModel->getUpdates($date);
        echo json_encode($data, JSON_UNESCAPED_UNICODE);

    }

    public function updatecheck_post(){
        
        $date = $this->post('date');
        $data = $this->BasicModel->areUpdatesAvailable($date);
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        
    }

}