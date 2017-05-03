<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class BillModel extends CI_Model{

    function __construct(){
        parent::__construct();
    }
    
    public function getUnbilledOrders(){

        $statement = "SELECT * FROM order_ WHERE order_state_id = 3 OR order_state_id = 5 OR order_state_id = 6";
        $query = $this->db->query($statement);
        $results = $query->result_array();

        $object = new stdClass();
        $object->orders = $results;

        $orderItems = array();

        for ($i=0; $i < sizeof($results); $i++) {
            $orderId = $results[$i]->id;
            $statement = "SELECT * FROM order_items WHERE order_id = " . $orderId;
            $query = $this->db->query($statement);
            $itemResults = $query->result_array();

            for ($j=0; $j < sizeof($itemResults); $j++) {
                $itemResult = $itemResults[$j];
                array_push($orderItems, $itemResult);
            }

        }

        $object->order_items = $orderItems;

        return $object;
    }

    public function updateOrders($orders){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $error = true;
        $stateQuery = "UPDATE order_ SET order_state_id = ?, biller_id = ?, modified = NOW() WHERE id = ?";

        foreach($orders as $json) {
            $error = $this->db->query($stateQuery, array($json->state_id, $json->biller_id, $json->id));
        }

        return $error;

    }

    public function updateOrder($id, $stateId, $billerId){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $stateQuery = "UPDATE order_ SET order_state_id = ?, biller_id = ?, modified = NOW() WHERE id = ?";
        return $this->db->query($stateQuery, array($stateId, $billerId, $id));

    }

    public function finishOrder($id, $stateId, $billerId, $orderItems){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $error = false;

        $stateQuery = "UPDATE order_ SET order_state_id = ?, biller_id = ?, modified = NOW() WHERE id = ?";
        $this->db->query($stateQuery, array($stateId, $billerId, $id));

        foreach($orderItems as $json) {

            $getQuery = "SELECT * FROM item WHERE id = " . $json->item_id;
            $query = $this->db->query($getQuery);

            foreach ($query->result() as $row)
            {
                $availableUnits = $row->available_units;
                $minimumUnits = $row->minimum_available;
                $name = $row->name;
                $itemId = $row->id;
            }

            $updateQuery = "UPDATE item SET available_units = (? - ?), modified = NOW() WHERE id = ?";
            $this->db->query($updateQuery, array($availableUnits, $json->storage_units, $json->item_id));

            $insertQuery = "INSERT INtO item_disposal(concept, units, item_id, total, disposal_date) VALUES (?,?,?,?, NOW())";
            $concept = "Venta del producto";
            $units = $json->storage_units;
            $total = $json->total;
            $error = $this->db->query($insertQuery, array($concept, $units, $json->item_id, $total));

            if (($availableUnits-$minimumUnits) <= $minimumUnits) {
                $message = "El limite minimo (". $minimumUnits .") del producto " . $name . " con codigo " .$itemId . " ha sido excedido";
                $insertQuery = "INSERT INtO messages (message) VALUES (?)";
                $error = $this->db->query($insertQuery, array($message));
            }

            if (($availableUnits-$minimumUnits) <= 0) {
                $message = "El producto " . $name . " con codigo " .$itemId . " se ha acabado, recomendamos que adquiera mas unidades lo antes posible";
                $insertQuery = "INSERT INtO messages (message) VALUES (?)";
                $error = $this->db->query($insertQuery, array($message));
            }

        }

        return $error;

    }

}