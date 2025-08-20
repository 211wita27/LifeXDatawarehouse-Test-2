import React from "react";
import ReactDOM from "react-dom/client";
import "bootstrap/dist/css/bootstrap.min.css";
import "./index.css";
import Content from "./components/Content";
import Sidebar from "./components/Sidebar";
import HelpColumn from "./components/HelpColumn";
import Header from "./components/Header";

const App: React.FC = () => {
    return (
        <>
            <a className="visually-hidden-focusable" href="#main">Skip to content</a>
    <Header />
    <div className="container-xxl py-3">
    <div className="row g-3">
    <aside className="col-12 col-xl-3 order-2 order-xl-1">
        <Sidebar />
        </aside>
        <main id="main" className="col-12 col-xl-6 order-1 order-xl-2">
        <Content />
        </main>
        <aside className="col-12 col-xl-3 order-3">
        <HelpColumn />
        </aside>
        </div>
        </div>
        </>
);
};

ReactDOM.createRoot(document.getElementById("root")!).render(<App />);