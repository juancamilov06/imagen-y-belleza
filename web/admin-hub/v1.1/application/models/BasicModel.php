<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class BasicModel extends CI_Model
{

    function __construct(){
        parent::__construct();
    }

    public function login($username, $password){

        $getQuery = "SELECT * FROM user WHERE username = '" . $username . "' AND identificator = '" . $password ."' AND is_active = 1";
        $query = $this->db->query($getQuery);
        $results = $query->row_array();

        return $results;

    }
    
    public function getUnreadMessages(){

        $statement = "SELECT * FROM messages WHERE is_read = 0";
        $query = $this->db->query($statement);
        $results = $query->result_array();

        $messages = new stdClass();
        $messages->messages = $results;
        return $messages;

    }
    
    public function setAllRead(){

        $statement = "UPDATE messages SET is_read = 1";
        return $this->db->query($statement);
        
    }

    public function getAllMessages(){

        $statement = "SELECT * FROM messages ORDER BY is_read";
        $query = $this->db->query($statement);
        $results = $query->result_array();

        $messages = new stdClass();
        $messages->messages = $results;
        return $messages;

    }

    public function getAll(){

        $versionQuery = "SELECT date FROM version";
        $query = $this->db->query($versionQuery);

        $lastModified = null;

        foreach ($query->result() as $itemRow) {
            $lastModified = $itemRow->date;
        }

        $clientsQuery = "SELECT * FROM client";
        $query = $this->db->query($clientsQuery);
        $results = $query->result_array();

        $object = new stdClass();
        $object->clients = $results;

        $clientsTypeQuery = "SELECT * FROM client_type";
        $query = $this->db->query($clientsTypeQuery);
        $results = $query->result_array();

        $object->types = $results;

        $orderStatesQuery = "SELECT * FROM order_state";
        $query = $this->db->query($orderStatesQuery);
        $results = $query->result_array();

        $object->order_states = $results;

        $paymentQuery = "SELECT * FROM payment";
        $query = $this->db->query($paymentQuery);
        $results = $query->result_array();

        $object->payments = $results;

        $orderItemStatesQuery = "SELECT * FROM order_items_state";
        $query = $this->db->query($orderItemStatesQuery);
        $results = $query->result_array();

        $object->order_item_states = $results;

        $itemsQuery = "SELECT * FROM item";
        $query = $this->db->query($itemsQuery);
        $results = $query->result_array();

        $object->items = $results;

        $usersQuery = "SELECT * FROM user ";
        $query = $this->db->query($usersQuery);
        $results = $query->result_array();

        $object->users = $results;

        $cityQuery = "SELECT * FROM city ";
        $query = $this->db->query($cityQuery);
        $results = $query->result_array();

        $object->cities = $results;

        $brandsQuery = "SELECT * FROM brand ";
        $query = $this->db->query($brandsQuery);
        $results = $query->result_array();

        $object->brands = $results;

        $categoryQuery = "SELECT * FROM category ";
        $query = $this->db->query($categoryQuery);
        $results = $query->result_array();

        $object->categories = $results;

        $orderQuery = "SELECT * FROM order_ ";
        $query = $this->db->query($orderQuery);
        $results = $query->result_array();

        $object->orders = $results;

        $orderQuery = "SELECT * FROM order_items ";
        $query = $this->db->query($orderQuery);
        $results = $query->result_array();

        $object->order_items = $results;

        $object->last_modified = $lastModified;

        return $object;
    }

    public function getUpdates($date){

        $versionQuery = "SELECT date FROM version";
        $query = $this->db->query($versionQuery);
        $results = (object) $query->row_array();

        $lastModified = $results->date;

        $clientsQuery = "SELECT * FROM client WHERE modified > '" . $date . "'";
        $query = $this->db->query($clientsQuery);
        $results =  $query->result_array();

        $object = new stdClass();
        $object->clients = $results;

        $itemsQuery = "SELECT * FROM item WHERE modified > '" . $date . "'";
        $query = $this->db->query($itemsQuery);
        $results =  $query->result_array();

        $object->items = $results;

        $usersQuery = "SELECT * FROM user WHERE modified > '" . $date . "'";
        $query = $this->db->query($usersQuery);
        $results =  $query->result_array();

        $object->users = $results;

        $cityQuery = "SELECT * FROM city WHERE modified > '" . $date . "'";
        $query = $this->db->query($cityQuery);
        $results =  $query->result_array();

        $object->cities = $results;

        $brandsQuery = "SELECT * FROM brand WHERE modified > '" . $date . "'";
        $query = $this->db->query($brandsQuery);
        $results =  $query->result_array();

        $object->brands = $results;

        $categoryQuery = "SELECT * FROM category WHERE modified > '" . $date . "'";
        $query = $this->db->query($categoryQuery);
        $results =  $query->result_array();

        $object->categories = $results;

        $orderQuery = "SELECT * FROM order_ WHERE modified > '" . $date . "'";
        $query = $this->db->query($orderQuery);
        $results =  $query->result_array();

        $object->orders = $results;

        $orderItemsQuery = "SELECT * FROM order_items WHERE modified > '" . $date . "'";
        $query = $this->db->query($orderItemsQuery);
        $results =  $query->result_array();

        $object->order_items = $results;
        $object->last_modified = $lastModified;

        return $object;

    }
    
    public function areUpdatesAvailable($date){

        $versionQuery = "SELECT date FROM version";
        $query = $this->db->query($versionQuery);

        $lastModified = null;

        foreach ($query->result() as $itemRow) {
            $lastModified = $itemRow->date;
        }

        if ($lastModified > $date) {

            $count = 0;

            $clientsQuery = "SELECT COUNT(*) as count FROM client WHERE modified > '" . $date . "'";
            $query = $this->db->query($clientsQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $itemsQuery = "SELECT COUNT(*) as count FROM item WHERE modified > '" . $date . "'";
            $query = $this->db->query($itemsQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $usersQuery = "SELECT COUNT(*) as count FROM user WHERE modified > '" . $date . "'";
            $query = $this->db->query($usersQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $cityQuery = "SELECT COUNT(*) as count FROM city WHERE modified > '" . $date . "'";
            $query = $this->db->query($cityQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $brandsQuery = "SELECT COUNT(*) as count FROM brand WHERE modified > '" . $date . "'";
            $query = $this->db->query($brandsQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $categoryQuery = "SELECT COUNT(*) as count FROM category WHERE modified > '" . $date . "'";
            $query = $this->db->query($categoryQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $orderQuery = "SELECT COUNT(*) as count FROM order_ WHERE modified > '" . $date . "'";
            $query = $this->db->query($orderQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $orderItemsQuery = "SELECT COUNT(*) as count FROM order_items WHERE modified > '" . $date . "'";
            $query = $this->db->query($orderItemsQuery);
            $results = (object) $query->row_array();

            $count = $count + $results->count;

            $object = new stdClass();
            $object->count = $count;

            return $object;

        } else {

            $object = new stdClass();
            $object->update = "no";
            
            return $object;

        }

    }
    
}