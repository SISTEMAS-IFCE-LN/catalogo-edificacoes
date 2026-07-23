import {HomePage} from "@/routes/home/page"
import {Toaster} from "@/components/ui/sonner"

export default function App() {
    return (
        <>
            <HomePage/>
            <Toaster richColors position="top-right"/>
        </>
    )
}