<?php

if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class OrderItemModel extends CI_Model{

    function __construct(){
        parent::__construct();
    }

    public function getOrderItems($orderId){

        $orderItemsQuery = "SELECT * FROM order_items WHERE order_id = " . $orderId . " AND subitem_id = 0 ";
        $query = $this->db->query($orderItemsQuery);

        $object = new stdClass();
        $object->data = array();

        foreach ($query->result() as $row) {

            $itemId = $row->item_id;
            $packerId = $row->packer_id;
            $orderItemStateId = $row->order_items_state_id;

            $stateQuery = "SELECT state FROM order_items_state WHERE id = " . $orderItemStateId;
            $stateQueryResult = $this->db->query($stateQuery);
            $stateResults = (object) $stateQueryResult->row_array();

            $row->order_items_state_id = $stateResults->state;

            if ($row->subitem_name === "") {
                $itemQuery = "SELECT name FROM item WHERE id = " . $itemId;
                $itemQueryResult = $this->db->query($itemQuery);
                $itemResults = (object) $itemQueryResult->row_array();

                $row->subitem_name = $itemResults->name;
            }

            if ($packerId === null) {
                $row->packer_id = 'No aplica';
            } else {
                $userQuery = "SELECT contact FROM user WHERE id = " . $packerId;
                $userQueryResult = $this->db->query($userQuery);
                $userResults = (object) $userQueryResult->row_array();

                $row->biller_id = $userResults->contact;
            }

            $orderItemsQuery = "SELECT * FROM order_items WHERE order_id = " . $orderId . " AND item_id = " . $itemId . " AND subitem_id != 0 ";
            $orderItemsQueryResult = $this->db->query($orderItemsQuery);

            $subitemsString = "";

            foreach ($orderItemsQueryResult->result() as $subitem) {
                $subitemsString = $subitemsString . $subitem->subitem_name . " * " . ($subitem->free_units + $subitem->units) . ", ";
            }
            if ($subitemsString != "") {
                $subitemsString = substr($subitemsString, 0, -2);
            } else {
                $subitemsString = "-";
            }

            $row->items = $subitemsString;
            array_push($object->data, $row);
        }

        return $object->data;

    }
    
    public function create($object){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $date = date('Y-m-d', time());
        $error = true;
        $insertQuery = "INSERT INTO order_items(order_id, subitem_id, unit_price, units, free_units, notes, packer_id, order_items_state_id, item_id, iva, discount, total, value, modified, subitem_name, storage_units, storage_notes, eq_value) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        foreach($object as $json) {
            if($this->db->query($insertQuery, array(
                $json->order_id,
                $json->subitem_id,
                $json->unit_price,
                $json->units,
                $json->free_units,
                $json->notes,
                null,
                $json->order_items_state_id,
                $json->item_id,
                $json->iva,
                $json->discount,
                $json->total,
                $json->value,
                $date,
                $json->subitem_name,
                $json->storage_units,
                $json->storage_notes,
                $json->eq_value
            ))){
                $error = true;
            } else {
                $error = false;
            }
        }

        return $error;
    }

    public function updateOrders($orders){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE order_ SET order_state_id = ?, modified = NOW() WHERE id = ?";
        $error = false;

        foreach($orders as $json) {
            if($this->db->query($updateQuery, array(
                $json->order_state_id,
                $json->order_id
            ))){
                $error = true;
            } else {
                $error = false;
            }
        }
        return $error;
    }

    public function updateItems($object){

        $versionQuery = "UPDATE version SET date = NOW() WHERE id = 1";
        $this->db->query($versionQuery);

        $updateQuery = "UPDATE order_items SET storage_units = ?, storage_notes = ?, order_items_state_id = ?, units = ?, free_units = ?, total = ?, modified = NOW() WHERE order_id = ? AND item_id = ? AND subitem_id = ?";
        $error = false;

        foreach($object as $json) {
            if($this->db->query($updateQuery, array(
                $json->storage_units,
                $json->storage_notes,
                $json->order_items_state_id,
                $json->order_id,
                $json->item_id,
                $json->subitem_id,
                $json->units,
                $json->free_units,
                $json->total
            ))){
                $error = true;
            } else {
                $error = false;
            }
        }
        return $error;
    }

}