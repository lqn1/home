package org.example.ncy.dataanalyst.demos.entity;

import javax.persistence.*;

/**
 * 记录目录分类及分组情况
 */

@Entity
@Table(name="directorytype")
public class DirectoryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//主键
    private Long fatherid;//父节点
    private String icon;//节点类型 package包/data/表
    private String tablename;//表名
    private String translate;//翻译
    private String createtime;//创建时间
    private String updatetime;//最近修改时间

    public DirectoryType() {}

    public DirectoryType(Long id, Long fatherid, String icon, String tablename, String translate, String createtime, String updatetime) {
        this.id = id;
        this.fatherid = fatherid;
        this.icon = icon;
        this.tablename = tablename;
        this.translate = translate;
        this.createtime = createtime;
        this.updatetime = updatetime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFatherid() {
        return fatherid;
    }

    public void setFatherid(Long fatherid) {
        this.fatherid = fatherid;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public String toString() {
        return "DirectoryType{" +
                "id=" + id +
                ", fatherid=" + fatherid +
                ", icon='" + icon + '\'' +
                ", tablename='" + tablename + '\'' +
                ", translate='" + translate + '\'' +
                ", createtime='" + createtime + '\'' +
                ", updatetime='" + updatetime + '\'' +
                '}';
    }
}
