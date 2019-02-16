package jp.co.shiratsuki.walkietalkie.bean;

/**
 * 部门实体类
 * Created at 2019/2/16 14:07
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class Department {

    private int department_id;

    private String department_name;

    public int getDepartment_id() {
        return department_id;
    }

    public void setDepartment_id(int department_id) {
        this.department_id = department_id;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    @Override
    public String toString() {
        return "Department [department_id=" + department_id + ", department_name=" + department_name + "]";
    }

}
