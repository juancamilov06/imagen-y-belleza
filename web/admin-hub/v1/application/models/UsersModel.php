<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class UsersModel extends CI_Model{

    function __construct(){
        parent::__construct();
    }

    public function getSellerByOrder($orderId){

        $object = new stdClass();
        $object->data = array();

        $getQuery = "SELECT seller_id, client_id, payment_id, deliver, notes, payment_notes FROM order_ WHERE id = " . $orderId;
        $query = $this->db->query($getQuery);
        $results = (object) $query->row_array();

        $sellerId = $results->seller_id;
        $clientId = $results->client_id;

        $clientQuery = "SELECT code, nit, company, contact, address, neighborhood, city_id, client_type_id, phone_one, phone_two, phone_three FROM client WHERE id = " . $clientId;
        $query = $this->db->query($clientQuery);
        $clientResults = (object) $query->row_array();

        $sellerQuery = "SELECT contact FROM user WHERE id = " . $sellerId;
        $query = $this->db->query($sellerQuery);
        $sellerResults = (object) $query->row_array();

        $results->seller_id = $sellerResults->contact;
        $results->code = $clientResults->code;
        $results->nit = $clientResults->nit;
        $results->company = $clientResults->company;
        $results->client_name = $clientResults->contact;
        $results->address = $clientResults->address;
        $results->neighborhood = $clientResults->neighborhood;
        $results->phone_one = $clientResults->phone_one;
        $results->phone_two = $clientResults->phone_two;
        $results->phone_three = $clientResults->phone_three;

        $paymentQuery = "SELECT term, name FROM payment WHERE id = " . $results->payment_id;
        $query = $this->db->query($paymentQuery);
        $paymentResults = (object) $query->row_array();

        $results->payment_id = $paymentResults->name . ' a ' . $paymentResults->term . ' dias';

        $cityQuery = "SELECT city FROM city WHERE id = " . $clientResults->city_id;
        $query = $this->db->query($cityQuery);
        $cityResults = (object) $query->row_array();

        $results->city = $cityResults->city;

        $typeQuery = "SELECT name FROM client_type WHERE id = " . $clientResults->client_type_id;
        $query = $this->db->query($typeQuery);
        $typeResults = (object) $query->row_array();

        $results->type = $typeResults->name;
        array_push($object->data, $results);

        return $object->data;

    }
    
    public function getUsers(){

        $selectQuery = "SELECT * FROM user WHERE is_active = 1";
        $query = $this->db->query($selectQuery);
        $results = $query->result_array();

        return $results;
    }

    public function getUserById($id){

        $getQuery = "SELECT * FROM user WHERE id = " . $id;
        $query = $this->db->query($getQuery);
        $results = (object) $query->row_array();

        return $results;

    }

    public function updatePassword($password, $id){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $selectQuery = "UPDATE user SET identificator = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($selectQuery, array($password, $id));

    }

    public function updateUser($json){
        
        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE user SET username = ?, is_active = ?, contact = ?, role = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($updateQuery, array($json->username, $json->is_active
        , $json->contact, $json->role, $json->id));

    }

}