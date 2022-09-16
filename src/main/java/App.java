import controller.DvdLibraryController;
import dao.DvdLibraryDao;
import dao.DvdLibraryDaoFileImpl;
import ui.DvdLibraryView;
import ui.UserIO;
import ui.UserIOImpl;

public class App {
    public static void main(String[] args) {
        UserIO myIo = new UserIOImpl();
        DvdLibraryView myView = new DvdLibraryView(myIo);
        DvdLibraryDao myDao = new DvdLibraryDaoFileImpl();
        DvdLibraryController controller = new DvdLibraryController(myDao, myView);

        controller.run();
    }
}