<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class OrderModel extends CI_Model{

    function __construct(){
        parent::__construct();
    }

    public function create($id, $made, $deliver, $notes, $payment_id, $sellerId, $clientId, $orderStateId){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $date = date('Y-m-d', time());
        $insertQuery = "INSERT INTO order_(id, made, deliver, modified, notes, seller_id, client_id, order_state_id, biller_id, payment_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
        return $this->db->query($insertQuery, array(
            $id,
            $made,
            $deliver,
            $date,
            $notes,
            $sellerId,
            $clientId,
            $orderStateId,
            null,
            $payment_id
        ));

    }

    public function getOrderDetail($orderId){

        $getQuery = "SELECT seller_id, client_id, payment_id, deliver, notes, payment_notes FROM order_ WHERE id = " . $orderId;
        $query = $this->db->query($getQuery);
        $results = (object) $query->row_array();

        $sellerId = $results->seller_id;
        $clientId = $results->client_id;

        $clientQuery = "SELECT code, nit, company, contact, address, neighborhood, city_id, client_type_id, phone_one, phone_two, phone_three FROM client WHERE id = " . $clientId;
        $query = $this->db->query($clientQuery);
        $clientResults = (object) $query->row_array();

        $results->code = $clientResults->code;
        $results->nit = $clientResults->nit;
        $results->company = $clientResults->company;
        $results->client_name = $clientResults->contact;
        $results->address = $clientResults->address;
        $results->neighborhood = $clientResults->neighborhood;
        $results->phone_one = $clientResults->phone_one;
        $results->phone_two = $clientResults->phone_two;
        $results->phone_three = $clientResults->phone_three;

        $sellerQuery = "SELECT contact FROM user WHERE id = " . $sellerId;
        $query = $this->db->query($sellerQuery);
        $sellerResults = (object) $query->row_array();

        $results->seller_id = $sellerResults->contact;
        

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
        return $results;
        
    }

    public function createAll($object){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $date = date('Y-m-d', time());
        $error = true;
        $insertQuery = "INSERT INTO order_(id, made, deliver, modified, notes, seller_id, client_id, order_state_id, biller_id, payment_id, payment_notes) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        foreach($object as $json) {
            if($this->db->query($insertQuery, array(
                $json->id,
                $json->made,
                $json->deliver,
                $date,
                $json->notes,
                $json->seller_id,
                $json->client_id,
                $json->order_state_id,
                null,
                $json->payment_id,
                $json->payment_notes
            ))){
                $error = true;
            } else {
                $error = false;
            }
        }

        return $error;
    }

    public function getAllHub(){

        $getQuery = "SELECT * FROM order_ ORDER BY order_state_id, modified";
        $query = $this->db->query($getQuery);

        $object = new stdClass();
        $object->data = array();

        foreach ($query->result()as $row) {

            $clientId = $row->client_id;
            $billerId = $row->biller_id;
            $sellerId = $row->seller_id;
            $paymentId = $row->payment_id;
            $orderStateId = $row->order_state_id;

            $clientQuery = "SELECT company FROM client WHERE id = " . $clientId;
            $query = $this->db->query($clientQuery);
            $clientResults = (object) $query->row_array();

            $row->client_id = $clientResults->company;

            try{
                $billerQuery = "SELECT contact FROM user WHERE id = " . $billerId;
                $query = $this->db->query($billerQuery);
                $billerResults = (object) $query->row_array();
                $row->biller_id = $billerResults->contact;
            } catch(PDOException $e){

            }

            $userQuery = "SELECT contact FROM user WHERE id = " . $sellerId;
            $query = $this->db->query($userQuery);
            $userResults = (object) $query->row_array();

            $row->seller_id = $userResults->contact;

            $stateQuery = "SELECT state FROM order_state WHERE id = " . $orderStateId;
            $query = $this->db->query($stateQuery);
            $stateResults = (object) $query->row_array();

            $row->order_state_id = $stateResults->state;

            $paymentQuery = "SELECT name, term FROM payment WHERE id = " . $paymentId;
            $query = $this->db->query($paymentQuery);
            $paymentResults = (object) $query->row_array();

            $row->payment_id = $paymentResults->name . ' a ' . $paymentResults->term . ' dias';
            
            array_push($object->data, $row);

        }

        return $object->data;

    }

    public function updateOrderHub($orderId){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);
        
        $orderQuery = "UPDATE order_ SET order_state_id = 12, modified = NOW() WHERE id = " . $orderId;
        return $this->db->query($orderQuery);
        
    }

    public function getOrders(){
        
        $statement = "SELECT * FROM order_ ORDER BY order_state_id" ;
        $query = $this->db->query($statement);
        $results = $query->result_array();

        $object = new stdClass();
        $object->orders = $results;

        $orderItems = array();

        foreach ($query->result() as $row) {
            $orderId = $row->id;
            $itemQuery = "SELECT * FROM order_items WHERE order_id = " . $orderId;
            $itemResults = $this->db->query($itemQuery);

            foreach ($itemResults->result() as $itemRow) {
                array_push($orderItems, $itemRow);
            }
        }

        $object->order_items = $orderItems;

        return $object;
    }

    public function getStorageAll(){

        $storageQuery = "SELECT * FROM order_ WHERE order_state_id = 5";
        $query = $this->db->query($storageQuery);
        $results = $query->result_array();

        $object = new stdClass();
        $object->orders = $results;

        $orderItems = array();

        foreach ($query->result() as $row) {
            $orderId = $row->id;
            $itemQuery = "SELECT * FROM order_items WHERE order_id = " . $orderId;
            $itemResults = $this->db->query($itemQuery);

            foreach ($itemResults->result() as $itemRow) {
                array_push($orderItems, $itemRow);
            }
        }

        $object->order_items = $orderItems;
        return $object;
    }

}