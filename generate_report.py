import os
import requests
import base64
from fpdf import FPDF

def create_uml_image():
    uml = """classDiagram
    class WasteBin {
      -String assetID
      -String location
      -double maxCapacity
      -double currentFillLevel
      -boolean isCriticalAlert
      +addWaste(double)
      +emptyBin()
      +processWaste() String
    }
    class GeneralWasteBin {
      +processWaste() String
    }
    class RecyclableBin {
      -double paperWeight
      -double plasticWeight
      -double metalWeight
      +addWaste(double)
    }
    WasteBin <|-- GeneralWasteBin
    WasteBin <|-- RecyclableBin
    
    class Dispatchable {
      <<interface>>
      +deploy(WasteBin, RoutingMap) String
    }
    class GarbageTruck {
      +deploy(WasteBin, RoutingMap) String
    }
    class RecyclingVan {
      +deploy(WasteBin, RoutingMap) String
    }
    Dispatchable <|.. GarbageTruck
    Dispatchable <|.. RecyclingVan
    
    class RoutingMap {
      -Map graph
      +getShortestPathList(String) List
    }
    class SmartCityCommandCenter {
      -CopyOnWriteArrayList cityBins
      -RoutingMap routingMap
      -StatsManager statsManager
      +simulateTime(int)
    }
    SmartCityCommandCenter --> WasteBin
    SmartCityCommandCenter --> RoutingMap
    SmartCityCommandCenter --> Dispatchable
    """
    
    graphbytes = uml.encode("utf8")
    base64_bytes = base64.b64encode(graphbytes)
    base64_string = base64_bytes.decode("ascii")
    
    url = "https://mermaid.ink/img/" + base64_string + "?type=png"
    response = requests.get(url)
    if response.status_code == 200:
        with open("uml_diagram.png", "wb") as f:
            f.write(response.content)
            return True
    return False

class PDF(FPDF):
    def header(self):
        self.set_font("Helvetica", "B", 15)
        self.cell(0, 10, "Smart City Waste Management System", border=False, align="C")
        self.ln(20)
        
    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "I", 8)
        self.cell(0, 10, f"Page {self.page_no()}", align="C")
        
    def chapter_title(self, num, title):
        self.set_font("Helvetica", "B", 12)
        self.set_fill_color(200, 220, 255)
        self.cell(0, 10, f"{num}. {title}", fill=True, ln=True)
        self.ln(4)
        
    def chapter_body(self, text):
        self.set_font("Helvetica", "", 11)
        self.multi_cell(0, 6, text)
        self.ln(10)

def generate_pdf():
    has_image = create_uml_image()
    
    pdf = PDF()
    pdf.add_page()
    
    pdf.chapter_title(1, "Introduction & SDG Background")
    intro_text = (
        "The Smart City Waste Management System is a comprehensive Java-based simulation designed to address "
        "urban sanitation challenges. It aligns directly with the UN's Sustainable Development Goal (SDG) 11: "
        "Sustainable Cities and Communities. By optimizing waste collection routes and providing real-time data "
        "analytics, the system aims to reduce the carbon footprint of sanitation fleets while ensuring cities "
        "remain clean and visually appealing."
    )
    pdf.chapter_body(intro_text)
    
    pdf.chapter_title(2, "Problem Statement")
    prob_text = (
        "Traditional municipal waste collection relies on static, predetermined routes regardless of the actual "
        "fill levels of the bins. This inefficiency leads to two major issues: overflowing bins in high-traffic "
        "areas (causing health hazards and visual pollution) and unnecessary fuel consumption when trucks are "
        "dispatched to collect nearly empty bins. There is a critical need for a dynamic, data-driven approach."
    )
    pdf.chapter_body(prob_text)
    
    pdf.chapter_title(3, "System Objectives")
    obj_text = (
        "- Monitor live fill levels of both General Waste and Recyclable bins.\\n"
        "- Trigger automated, real-time dispatches only when bins reach a critical threshold (>85%).\\n"
        "- Optimize travel distances using graph theory (Dijkstra's Algorithm) to find the shortest path.\\n"
        "- Provide visual analytics regarding waste generation trends, geographic hot-spots, and recyclable composition."
    )
    obj_text = obj_text.replace("\\n", "\n")
    pdf.chapter_body(obj_text)
    
    pdf.add_page()
    pdf.chapter_title(4, "OOP Design (UML Class Diagram)")
    oop_text = (
        "The system strictly adheres to Object-Oriented principles. Inheritance is utilized through the WasteBin "
        "base class and its specific derivatives (GeneralWasteBin, RecyclableBin). Polymorphism is demonstrated "
        "via the Dispatchable interface, implemented by GarbageTruck and RecyclingVan, allowing the SimulationEngine "
        "to deploy different vehicles identically."
    )
    pdf.chapter_body(oop_text)
    if has_image:
        pdf.image("uml_diagram.png", w=160)
        pdf.ln(10)
        
    pdf.add_page()
    pdf.chapter_title(5, "Implementation Details")
    imp_text = (
        "The application is built entirely in Java (Swing/AWT) with zero external dependencies. Key features include:\\n"
        "- Routing Graph: A custom Adjacency List representing Cyberjaya (Depot, DPULZE Mall, MMU, etc.) utilizing Dijkstra's Algorithm.\\n"
        "- Multi-threading: A javax.swing.Timer drives background auto-play simulation while retaining UI responsiveness.\\n"
        "- Data Persistence: Configuration and Lifetime Statistics are saved locally via CSV and Properties files.\\n"
        "- Custom Rendering: Line Charts, Bar Charts, Pie Charts, and the Live Tracking Node Map are rendered natively using Graphics2D."
    )
    imp_text = imp_text.replace("\\n", "\n")
    pdf.chapter_body(imp_text)
    
    pdf.chapter_title(6, "Testing & Sample Outputs")
    test_text = (
        "Testing was conducted via a highly accelerated time-lapse simulation (e.g., +1 MONTH skip). "
        "The custom Line Chart correctly visualized 30 days of cumulative waste data, appropriately reflecting "
        "randomly injected Weather events (Heavy Rain reducing waste) and Weekend Events (spiking waste by 50%). "
        "Furthermore, active trucks were visually verified moving node-by-node on the map canvas exactly as dictated "
        "by the shortest-path algorithm."
    )
    pdf.chapter_body(test_text)
    
    pdf.chapter_title(7, "Discussion & Limitations")
    disc_text = (
        "While the simulation successfully models a smart city, there are limitations. The routing map is "
        "currently hardcoded to 7 nodes in Cyberjaya. A real-world application would require integration with a "
        "live GPS/Maps API (e.g., Google Maps) to track real-time traffic and exact coordinate geography. "
        "Additionally, bin fill levels are simulated via random generation rather than hardware IoT sensors."
    )
    pdf.chapter_body(disc_text)
    
    pdf.chapter_title(8, "Conclusion")
    conc_text = (
        "The Smart City Command Center effectively proves that combining Object-Oriented design with algorithmic "
        "graph theory can dramatically optimize urban waste management. The system is highly scalable, visually "
        "informative, and directly addresses the core mission of SDG 11 by promoting sustainable, data-driven "
        "city infrastructure."
    )
    pdf.chapter_body(conc_text)
    
    pdf.output("Project_Report.pdf")

if __name__ == "__main__":
    generate_pdf()
