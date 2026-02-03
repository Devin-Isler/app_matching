public class Services {

    Service paint;
    Service web_dev;
    Service graphic_design;
    Service data_entry;
    Service tutoring;
    Service cleaning;
    Service writing;
    Service photography;
    Service plumbing;
    Service electrical;

    Service[] serviceList;

    Services(){
        this.paint = new Service(70, 60, 50, 85, 90, "paint");
        this.web_dev = new Service(95, 75, 85, 80, 90, "web_dev");
        this.graphic_design = new Service(75, 85, 95, 70, 85, "graphic_design");
        this.data_entry = new Service(50, 50, 30, 95, 95, "data_entry");
        this.tutoring = new Service(80, 95, 70, 90, 75, "tutoring");
        this.cleaning = new Service(40, 60, 40, 90, 85, "cleaning");
        this.writing = new Service(70, 85, 90, 80, 95,"writing");
        this.photography = new Service(85, 80, 90, 75, 90,"photography");
        this.plumbing = new Service(85, 65, 60, 90, 85, "plumbing");
        this.electrical = new Service(90, 65, 70, 95, 95, "electrical");
        this.serviceList = new Service[]{paint, web_dev, graphic_design, data_entry,
                tutoring, cleaning, writing, photography, plumbing, electrical};

    }

    public Service find(String name){
        for (Service service : serviceList) {
            if (service.name.compareTo(name) == 0) {
                return service;
            }
        }
        return null;
    }
}
