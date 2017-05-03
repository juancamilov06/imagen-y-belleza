<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

require APPPATH . "/libraries/REST_Controller.php";
use Restserver\Libraries\REST_Controller;

class ClientsMiddleware extends REST_Controller {

    public function __construct(){
        parent::__construct();
        $this->load->model('ClientModel');
    }

    public function index_get(){

        $this->load->view("clients");

    }

    public function update_post(){

        $client = json_decode($this->post('client'));
        $response = $this->ClientModel->updateClient($client);
        if ($response) {
            print 'Actualizado';
        } else {
            print 'Error';
        }

    }

    public function all_get(){

        $clients = $this->ClientModel->getClients();
        echo json_encode($clients, JSON_UNESCAPED_UNICODE);

    }

    public function find_get($id){

        $client = $this->ClientModel->getClientById($id);
        echo json_encode($client, JSON_UNESCAPED_UNICODE);

    }

    public function create_post(){

        $response = $this->ClientModel->create(
            $this->post('id'),
            $this->post('company'),
            $this->post('address'),
            $this->post('city_id'),
            $this->post('phone_one'),
            $this->post('phone_two'),
            $this->post('phone_three'),
            $this->post('nit'),
            $this->post('mail'),
            $this->post('contact'),
            $this->post('client_type_id'),
            $this->post('neighborhood'),
            $this->post('user_id'),
            $this->post('latitude'),
            $this->post('longitude')
        );

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

    public function createall_post(){

        $clients = json_decode($this->post('clients'));
        $response = $this->ClientModel->createAll($clients);

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

    public function typesupdate_post(){

        $clientId = $this->post('client_id');
        $typeId = $this->post('type_id');
        $latitude = $this->post('latitude');
        $longitude = $this->post('longitude');

        $response = $this->ClientModel->updateType($typeId, $clientId, $latitude, $longitude);
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

    public function types_get(){

        $types = $this->ClientModel->getTypes();
        echo json_encode($types, JSON_UNESCAPED_UNICODE);

    }

}